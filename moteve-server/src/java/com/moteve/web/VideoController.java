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
import com.moteve.service.VideoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Radek Skokan
 */
@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;

    private static final Logger logger = Logger.getLogger(VideoController.class);

    @RequestMapping(value = "/video/listVideos.htm", method = RequestMethod.GET)
    public ModelAndView showForm(HttpServletRequest request) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<Video> videos = videoService.getRecentVideos(10, request.getRemoteUser());
        model.put("videos", videos);
        model.put("currentUserEmail", request.getRemoteUser());
        return new ModelAndView("video/listVideos", model);
    }

    @RequestMapping(value = "/video/listVideos.htm", method = RequestMethod.POST)
    public ModelAndView findVideos(@RequestParam("videoName") String videoName,
            @RequestParam("author") String author,
            @RequestParam(value = "myVideos", required = false) boolean myVideos,
            @RequestParam("dateFrom") String dateFrom,
            @RequestParam("dateTo") String dateTo,
            @RequestParam(value = "live", required = false) boolean live) {
        logger.debug("Video search criteria: videoName=" + videoName + ", author=" + author
                + ", myVideos=" + myVideos + ", dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", live=" + live);

        return null;
    }
}
