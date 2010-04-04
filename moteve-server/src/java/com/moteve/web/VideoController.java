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
package com.moteve.web;

import com.moteve.domain.Video;
import com.moteve.domain.VideoPart;
import com.moteve.domain.VideoSearchCriteria;
import com.moteve.service.VideoService;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Radek Skokan
 */
// TODO: add validations, probably add Spring form binders
@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;

    private static final Logger logger = Logger.getLogger(VideoController.class);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/video/listVideos.htm", method = RequestMethod.GET)
    public ModelAndView showListForm(HttpServletRequest request) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<Video> videos = videoService.getRecentVideos(10, request.getRemoteUser());
        model.put("videos", videos);
        model.put("currentUserEmail", request.getRemoteUser());
        return new ModelAndView("video/listVideos", model);
    }

    @RequestMapping(value = "/video/listVideos.htm", method = RequestMethod.POST)
    public ModelAndView findVideos(HttpServletRequest request,
            @RequestParam("videoName") String videoName,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "myVideos", required = false) boolean myVideos,
            @RequestParam("dateFrom") String dateFrom,
            @RequestParam("dateTo") String dateTo,
            @RequestParam(value = "live", required = false) boolean live) {
        logger.debug("Video search criteria: videoName=" + videoName + ", author=" + author
                + ", myVideos=" + myVideos + ", dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", live=" + live);
        Map<String, Object> model = new ManagedMap<String, Object>();
        model.put("currentUserEmail", request.getRemoteUser());
        model.put("videoName", videoName);
        model.put("author", author);
        model.put("myVideos", myVideos);
        model.put("dateFrom", dateFrom);
        model.put("dateTo", dateTo);
        model.put("live", live);

        VideoSearchCriteria criteria = new VideoSearchCriteria();

        criteria.setLive(live);
        criteria.setVideoNamePattern(videoName);
        if (myVideos && request.getRemoteUser() != null) {
            criteria.setAuthorEmail(request.getRemoteUser());
        } else {
            criteria.setAuthorPattern(author);
        }
        try {
            if (dateFrom != null & dateFrom.length() > 0) {
                criteria.setDateFrom(dateFormat.parse(dateFrom));
            }
            if (dateTo != null && dateTo.length() > 0) {
                criteria.setDateTo(dateFormat.parse(dateTo));
            }
        } catch (ParseException e) {
            // TODO: proper validation
        }
        model.put("videos", videoService.findVideos(request.getRemoteUser(), criteria));

        return new ModelAndView("video/listVideos", model);
    }

    @RequestMapping(value = "/video/editVideo.htm", method = RequestMethod.GET)
    public ModelAndView showEditForm(HttpServletRequest request,
            @RequestParam("id") Long videoId) {
        Video video = videoService.getVideo(videoId);
        String email = request.getRemoteUser();
        if (video == null || !video.getAuthor().getEmail().equals(email)) {
            return new ModelAndView("redirect:/");
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("video", video);
        model.put("availableGroups", videoService.getAvailableGroups(email, videoId));
        model.put("videoGroups", videoService.getVideoGroups(email, videoId));
        model.put("availableContacts", videoService.getAvailableContacts(email, videoId));
        model.put("videoContacts", videoService.getVideoContacts(email, videoId));

        return new ModelAndView("video/editVideo", model);
    }

    @RequestMapping(value = "/video/updateVideoSettings.htm", method = RequestMethod.POST)
    public String setVideoSettings(HttpServletRequest request,
            @RequestParam(value = "id", required = false) Long videoId,
            @RequestParam(value = "videoName", required = false) String videoName,
            @RequestParam(value = "videoContacts", required = false) List<Long> videoContactIds,
            @RequestParam(value = "videoGroups", required = false) List<Long> videoGroupIds) {
        videoService.updateVideo(request.getRemoteUser(), videoId, videoName, videoContactIds, videoGroupIds);
        return "redirect:/video/listVideos.htm";
    }

    @RequestMapping(value = "/video/removeVideo.htm", method = RequestMethod.POST)
    public String removeVideo(HttpServletRequest request,
            @RequestParam("id") Long videoId) {
        videoService.markForRemoval(request.getRemoteUser(), videoId);
        return "redirect:/video/listVideos.htm";
    }

    @RequestMapping(value = "/video/watchVideo.htm", method = RequestMethod.GET)
    public ModelAndView watchVideo(HttpServletRequest request,
            @RequestParam("id") Long videoId) {
        Video video = videoService.getVideo(videoId);
        if (video == null) {
            logger.error("No video for id=" + videoId);
            return new ModelAndView("redirect:/");
        }
        String email = request.getRemoteUser();

        // TODO: check permissions

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("video", video);
        model.put("email", email);
        String streamUrl = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/") + 1) + "videoStream.htm?id=" + videoId;
        model.put("streamUrl", streamUrl);
        return new ModelAndView("video/watchVideo", model);
    }

    @RequestMapping(value = "/video/videoStream.htm", method = RequestMethod.GET)
    public void streamVideo(HttpServletRequest request, HttpServletResponse resp,
            @RequestParam("id") Long videoId) {
        logger.debug("Request to stream videoId=" + videoId);
        Video video = videoService.getVideo(videoId);
        if (video == null) {
            logger.error("No video for id=" + videoId);
            return;
        }

        // TODO: persmission check

        VideoPart part;
        Long playerPartId = (Long) request.getSession().getAttribute("playerPartId");
        logger.debug("session playerPartId=" + playerPartId);
        if (playerPartId == null) {
            // start with the first video part
            part = video.getFirstPart();
        } else {
            part = videoService.getNextVideoPart(playerPartId);
        }

        if (part == null || part.getTargetLocation() == null) {
            // no next part or the part is not transcoded yet
            logger.debug("No next video part orthe part is not transcoded yet");
            request.getSession().removeAttribute("playerPartId");
            return;
        } else {
            playerPartId = part.getId();
            request.getSession().setAttribute("playerPartId", playerPartId);
        }
        logger.debug("playerPartId=" + playerPartId);

        // stream the file
        String filePath = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        try {
            resp.setContentType("video/x-flv");
            resp.addHeader("Cache-Control", "no-cache");
            resp.addHeader("Pragma", "no-cache");
            dos = new DataOutputStream(resp.getOutputStream());
            filePath = part.getTargetLocation();
            File f = new File(filePath);
            long totalSize = 0;
            if (f.exists()) {
                logger.debug("Streaming videoId=" + videoId + ", playerPartId=" + playerPartId + ", file=" + filePath);
                resp.setContentLength((int) f.length());
                fis = new FileInputStream(filePath);
                byte[] buffer = new byte[resp.getBufferSize()];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    totalSize += bytesRead;
                    dos.write(buffer, 0, bytesRead);
                }

            } else {
                logger.debug("Finished streaming of videoId=" + videoId + ", playerPartId=" + playerPartId + ", file=" + filePath + "; " + totalSize + " bytes");
            }

        } catch (IOException e) {
            logger.error("Error streaming videoId=" + videoId + ", playerPartId=" + playerPartId + ", file=" + filePath + ": " + e.getMessage(), e);
        } finally {
            try {
                if (dos != null) {
                    dos.flush();
                    dos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
            }
        }

        return;
    }
}
