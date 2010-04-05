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
import com.moteve.dao.UserDao;
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
import com.moteve.domain.VideoSearchCriteria;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
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

    private String destVideoPath;

    private String destFileSuffix;

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private VideoPartDao videoPartDao;

    @Autowired
    private MediaFormatDao mediaFormatDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TranscodingService transcodingService;

    @Required
    public void setSourceVideoPath(String sourceVideoPath) {
        this.sourceVideoPath = sourceVideoPath;
    }

    @Required
    public void setDestVideoPath(String destVideoPath) {
        this.destVideoPath = destVideoPath;
    }

    @Required
    public void setDestFileSuffix(String destFileSuffix) {
        this.destFileSuffix = destFileSuffix;
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
    public Video startRecording(User author, String mediaFormatName, String allowedGroupName)
            throws UnknownMediaFormatException {
        // check for media format; must already exist
        MediaFormat mediaFormat = null;
        try {
            mediaFormat = mediaFormatDao.findByName(mediaFormatName);
        } catch (NoResultException e) {
//            throw new UnknownMediaFormatException("The media format '" + mediaFormatName + "' is not recognized");
            logger.info("Creating a new media format: " + mediaFormatName);
            mediaFormat = new MediaFormat();
            mediaFormat.setName(mediaFormatName);
            mediaFormat = mediaFormatDao.store(mediaFormat);
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
        video.setRemoved(false);
        video.setMarkedForRemoval(false);
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
            part.setTargetLocation(prepareDestVideoFilePath(part));
            part.setTranscodingFailed(false);
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

        // notify the transcoder to start after the media file has been closed and no error has occured
        transcodingService.work();
    }

    private String prepareSrcVideoFilePath(VideoPart part) {
        String dir = sourceVideoPath + File.separator
                + part.getVideo().getAuthor().getId() + File.separator
                + part.getVideo().getId();
        new File(dir).mkdirs();
        return dir + File.separator + part.getId() + part.getVideo().getSourceFormat().getFileSuffix();
    }

    private String prepareDestVideoFilePath(VideoPart part) {
        String dir = destVideoPath + File.separator
                + part.getVideo().getAuthor().getId() + File.separator
                + part.getVideo().getId();
        new File(dir).mkdirs();
        return dir + File.separator + part.getId() + destFileSuffix;
    }

    /**
     * Returns recently added videos that the user has permissions for.
     * Admin can see all.
     * @param count maximum number of the returned videos
     * @param email identifies the user
     * @return
     */
    public List<Video> getRecentVideos(int count, String email) {
        try {
            if (email == null) {
                // anonymous user, display only PUBLIC videos
                return videoDao.findRecentPublic(count);
            } else {
                return videoDao.findRecent(count, email);
            }
        } catch (NoResultException e) {
            return new ArrayList<Video>();
        }

    }

    /**
     * Returns list of videos mathing the search criteria. The returned videos
     * are accessible to the calling user.
     * @param email identifies the calling user
     * @param criteria
     * @return videos mathing the criterias and accesss permissions
     */
    public List<Video> findVideos(String email, VideoSearchCriteria criteria) {
        try {
            return videoDao.findByCriteria(email, criteria);
        } catch (NoResultException e) {
            return new ArrayList<Video>();
        }
    }

    public Video getVideo(Long videoId) {
        try {
            return videoDao.findById(videoId);
        } catch (NoResultException e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * Finds contacts available for the given user and video. The result are
     * contacts that the user has and are not already associated with the specified video.
     * @param email identifies the user
     * @param videoId identifies the video
     * @return
     */
    public List<User> getAvailableContacts(String email, Long videoId) {
        if (email == null || videoId == null) {
            return new ArrayList<User>();
        }

        try {
            return videoDao.findAvailableVideoContacts(email, videoId);
        } catch (NoResultException e) {
            return new ArrayList<User>();
        }
    }

    /**
     * Returns user's contacts associated with a video.
     * @param email identifies the user
     * @param videoId identifies the video
     * @return
     */
    public List<User> getVideoContacts(String email, Long videoId) {
        if (email == null || videoId == null) {
            return new ArrayList<User>();
        }

        try {
            return videoDao.findVideoContacts(videoId);
        } catch (NoResultException e) {
            return new ArrayList<User>();
        }
    }

    /**
     * Finds groups available for the given user and video. The result are
     * groups that the user has and are not already associated with the specified video.
     * @param email identifies the user
     * @param videoId identifies the video
     * @return
     */
    public List<Group> getAvailableGroups(String email, Long videoId) {
        if (email == null || videoId == null) {
            return new ArrayList<Group>();
        }

        try {
            return videoDao.findAvailableVideoGroups(email, videoId);
        } catch (NoResultException e) {
            return new ArrayList<Group>();
        }
    }

    /**
     * Returns user's groups associated with a video.
     * @param email identifies the user
     * @param videoId identifies the video
     * @return
     */
    public List<Group> getVideoGroups(String email, Long videoId) {
        if (email == null || videoId == null) {
            return new ArrayList<Group>();
        }

        try {
            return videoDao.findVideoGroups(videoId);
        } catch (NoResultException e) {
            return new ArrayList<Group>();
        }
    }

    /**
     * Update the video name and access permissions
     * @param email the user performing the video update. Must be the video author
     * @param videoId
     * @param videoName new video name
     * @param videoContactIds list of contact IDs (users) that are allowed to watch the video
     * @param videoGroupIds list of new group IDs that are allowed to watch the video
     */
    public void updateVideo(String email, Long videoId, String videoName, List<Long> videoContactIds, List<Long> videoGroupIds) {
        try {
            Video video = videoDao.findById(videoId);
            if (!video.getAuthor().getEmail().equals(email)) {
                logger.error("Error updating video. User " + email + " is not the author of video ID=" + videoId);
                return;
            }

            video.setName(videoName);

            Set<Role> newPermissions = new HashSet<Role>();
            if (videoContactIds != null) {
                for (Long contactId : videoContactIds) {
                    User contact = userDao.findById(contactId);
                    newPermissions.add(contact);
                }
            }
            if (videoGroupIds != null) {
                for (Long groupId : videoGroupIds) {
                    Group group = groupDao.findById(groupId);
                    newPermissions.add(group);
                }
            }
            video.setPermissions(newPermissions);
            video = videoDao.store(video);
            logger.info("Update video ID=" + videoId + ": name=" + video.getName() + ", permissions=" + video.getPermissions());
        } catch (NoResultException e) {
            logger.error("Error updating video", e);
        }
    }

    /**
     * Mark the video for removal
     * @param email must be the video's author
     * @param videoId
     */
    public void markForRemoval(String email, Long videoId) {
        try {
            Video video = videoDao.findById(videoId);
            if (video.getAuthor().getEmail().equals(email)) {
                video.setMarkedForRemoval(true);
                videoDao.store(video);
                logger.info("Video ID=" + videoId + " has been marked for removal");
            }
        } catch (NoResultException e) {
            logger.error("Error marking video for removal", e);
        }
    }

    /**
     * Returns the successor of a video part.
     * @param partId identifies the current video part, which successor will be returned
     * @return the next video part; null if this is the last one
     */
    public VideoPart getNextVideoPart(Long partId) {
        try {
            VideoPart currPart = videoPartDao.findById(partId);
            return currPart.getNextPart();
        } catch (NoResultException e) {
            return null;
        }
    }
}
