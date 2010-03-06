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
import com.moteve.domain.VideoSearchCriteria;
import com.moteve.service.VideoService;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
    public ModelAndView showForm(HttpServletRequest request) {
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
}
