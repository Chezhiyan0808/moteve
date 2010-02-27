/*
 * Copyright 2009-2010 Moteve.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moteve.service;

import com.moteve.dao.GroupDao;
import com.moteve.dao.MediaFormatDao;
import com.moteve.dao.VideoDao;
import com.moteve.dao.VideoPartDao;
import com.moteve.domain.Group;
import com.moteve.domain.MediaFormat;
import com.moteve.domain.MoteveException;
import com.moteve.domain.Role;
import com.moteve.domain.UnknownMediaFormatException;
import com.moteve.domain.User;
import com.moteve.domain.Video;
import com.moteve.domain.VideoPart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Radek Skokan
 */
//@Service commented out as it needs some config params, so declared in XML
public class VideoService {

    public static final int BUFFER_SIZE = 8192;

    private static final Logger logger = Logger.getLogger(VideoService.class);

    private String sourceVideoPath;

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private VideoPartDao videoPartDao;

    @Autowired
    private MediaFormatDao mediaFormatDao;

    @Autowired
    private GroupDao groupDao;

    @Required
    public void setSourceVideoPath(String sourceVideoPath) {
        this.sourceVideoPath = sourceVideoPath;
    }

    /**
     * Creates a new video.
     * The flag InProgress will be set to true. After a new video is created by this
     * method, its parts can be recorded using addPart(). When the recording has
     * finished, the video should be closed by finishRecording().
     *
     * @param author the video author
     * @param mediaFormat a free-form text specifying the video media format;
     *          it already must be registered on the server
     * @param allowedGroupName the default group name that has access to watch the video.
     *          If the group does not exist for the user, it will be created.
     *          If it is null or empty, only the owner can watch the video.
     *          The special group name Group.PUBLIC allows anybody, including anonymous users,
     *          to watch the video.
     * @return the new Video
     * @throws UnknownMediaFormatException when the mediaFormat is not registered in DB
     */
    @Transactional
    public Video startRecording(User author, String mediaFormatName, String allowedGroupName)
            throws UnknownMediaFormatException {
        // check for media format; must already exist
        MediaFormat mediaFormat = null;
        try {
            mediaFormat = mediaFormatDao.findByName(mediaFormatName);
        } catch (NoResultException e) {
            throw new UnknownMediaFormatException("The media format '" + mediaFormatName + "' is not recognized");
        }

        // check the group existence
        // if the group is Group.JUST_ME, don't create the group and assign to it null instead
        Group group = null;
        if (!Group.JUST_ME.equals(allowedGroupName)) {
            try {
                group = groupDao.findByUserAndName(author.getId(), allowedGroupName);
            } catch (NoResultException e) {
                // the group does not exist yet - create
                logger.info("Creating a nonexisting group '" + allowedGroupName + "' for user " + author.getEmail());
                group = new Group();
                group.setName(allowedGroupName);
                group.setUser(author);
                group = groupDao.store(group);
            }
        }

        Video video = new Video();
        video.setSourceFormat(mediaFormat);
        video.setAuthor(author);
        video.setCreationDate(new Date());
        Set<Role> allowedRoles = new HashSet<Role>();
        allowedRoles.add(author);
        allowedRoles.add(group);
        video.setPermissions(allowedRoles);
        video.setRecordInProgress(true);
        video = videoDao.store(video);
        return video;
    }

    /**
     * Closes a video that is being recorded. Sets the video attribute
     * InProgress to false.
     * The specified user must be the video author.
     * @param user the video author
     * @param videoId the video that should be marked as finished
     * @throws MoteveException
     */
    @Transactional
    public void finishRecording(User user, Long videoId) throws MoteveException {
        if (user == null || videoId == null) {
            throw new MoteveException("Missing required arguments: user and/or videoId");
        }

        try {
            Video video = videoDao.findById(videoId);
            if (video.getAuthor().getId() == user.getId()) {
                logger.info("Finishing video recording: user=" + user.getEmail() + ", videoId=" + video.getId());
                video.setRecordInProgress(false);
                videoDao.store(video);
            } else {
                throw new MoteveException("Video id=" + videoId + " does not belong to user " + user.getEmail());
            }
        } catch (NoResultException e) {
            logger.error("Attempt to close a nonexisting video: user=" + user.getEmail() + ", videoId=" + videoId);
            throw new MoteveException("Attempt to close a nonexisting video: user=" + user.getEmail() + ", videoId=" + videoId);
        }
    }

    /**
     * Adds a part to an in-progress video.
     * When the part is added, a video transcoding process is initiated.
     *
     * @param videoId
     * @param inputStream
     * @exception MoteveException when the video part cannot be added
     */
    @Transactional
    public void addPart(Long videoId, InputStream inputStream) throws MoteveException {
        FileOutputStream fos = null;
        try {
            Video video = videoDao.findById(videoId);
            VideoPart part = new VideoPart();
            part.setCaptureTime(new Date());
            part.setVideo(video);
            part = videoPartDao.store(part); // save to obtain ID
            part.setSourceLocation(prepareSrcVideoFilePath(part)); // ID needed here
            videoPartDao.store(part); // update the part's source location

            // update link references
            if (part.getVideo().getFirstPart() == null) {
                part.getVideo().setFirstPart(part);
            }
            if (part.getVideo().getLastPart() != null) {
                part.getVideo().getLastPart().setNextPart(part);
            }
            part.getVideo().setLastPart(part);

            File f = null;
            f = new File(part.getSourceLocation());
            fos = new FileOutputStream(f);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            int totalSize = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalSize += bytesRead;
                fos.write(buffer, 0, bytesRead);
            }
            logger.info("Bytes written: " + totalSize + ". Total file (" + f.getAbsolutePath() + ") size=" + f.length() + " bytes");
        } catch (Exception e) {
            logger.error("Error adding video part", e);
            throw new MoteveException("Error adding video part: " + e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    private String prepareSrcVideoFilePath(VideoPart part) {
        String dir = sourceVideoPath + File.separator
                + part.getVideo().getAuthor().getId() + File.separator
                + part.getVideo().getId();
        new File(dir).mkdirs();
        return dir + File.separator + part.getId() + part.getVideo().getSourceFormat().getFileSuffix();
    }
}
