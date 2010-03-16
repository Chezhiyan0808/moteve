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

import com.moteve.domain.Group;
import com.moteve.domain.MoteveException;
import com.moteve.domain.User;
import com.moteve.domain.Video;
import com.moteve.service.UserService;
import com.moteve.service.VideoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller responsible for communication with Moteve Client Applications
 * (that run typically on mobile phones).
 *
 * @author Radek Skokan
 */
@Controller
public class McaController {

    private static final String DELIMITER = "\\";

    private static final Logger logger = Logger.getLogger(McaController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    /**
     * Before a Moteve Client Application (a mobile phone typically) starts any
     * communication with the server, it first needs to authenticate.
     * When the authentication process is successful, MCA is given a security
     * token that is then used when sending requests to the server.
     *
     * @param request contains HTTP header Moteve-Auth with email\password\device_description
     * @param response contains HTTP header Moteve-Token with the generated security token
     *          or AUTH_ERROR if the authentication failed. The token value is also in the
     *          response body.
     */
    @RequestMapping(value = "/mca/register.htm", method = RequestMethod.POST)
    public void registerMca(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Moteve-Auth");
        if (authHeader == null) {
            return;
        }

        int emailPasswordDelimiter = authHeader.indexOf(DELIMITER);
        int passwordDescDelimiter = authHeader.lastIndexOf(DELIMITER);
        if (emailPasswordDelimiter < 1 || passwordDescDelimiter < 1 || emailPasswordDelimiter == passwordDescDelimiter) {
            return;
        }

        String email = authHeader.substring(0, emailPasswordDelimiter);
        String password = authHeader.substring(emailPasswordDelimiter + 1, passwordDescDelimiter);
        String desc = authHeader.substring(passwordDescDelimiter + 1);

        logger.debug("Authenticating MCA. E-mail=" + email + ", desc=" + desc);
        User user = userService.authenticate(email, password);
        PrintWriter out = null;
        try {
            out = new PrintWriter(response.getOutputStream());
            response.setContentType("text/html");
            if (user == null) {
                logger.info("MCA authentication failed for user " + email);
                response.setHeader("Moteve-Token", "ERROR: Authentication failed");
                out.print("ERROR: Authentication failed");
            } else {
                logger.info("MCA authentication successful for user " + email);
                String token = userService.registerMca(user, desc);
                response.setHeader("Moteve-Token", token);
                out.print(token);
            }
        } catch (IOException e) {
            logger.error(e);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * Provides list with names of Groups that the user has configured on the server.
     *
     * @param request must contain HTTP header Moteve-Token with the value
     *          received during device registration
     * @param response the HTTP body contains list of group names the user has.
     *          The groups are delimited by a backslash (\) letter.
     */
    @RequestMapping(value = "/mca/listGroups.htm", method = RequestMethod.POST)
    public void listGroups(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(response.getOutputStream());
            response.setContentType("text/html");
            String token = request.getHeader("Moteve-Token");
            if (token == null) {
                response.setHeader("Moteve-Token", "ERROR: Missing token");
                out.print("ERROR: Missing token");
                return;
            }

            User user = userService.getUserForDevice(token);
            if (user == null) {
                response.setHeader("Moteve-Token", "ERROR: Wrong token");
                out.print("ERROR: Wrong token");
                return;
            }

            StringBuffer groupNames = new StringBuffer();
            for (Group group : user.getGroups()) {
                groupNames.append(group.getName()).append(DELIMITER);
            }

            groupNames.append(Group.JUST_ME);

            out.print(groupNames.toString());
        } catch (IOException e) {
            logger.error(e);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * HTTP POST method for uploading captured video parts.<br/>
     * The Process is:
     * <ul>
     * <li>Client sends headers: <code>Moteve-Sequence</code> containing <code>new</code>,
     * <code>Moteve-DefaultGroup</code> containing
     * and <code>Moteve-MediaFormat</code> containing the video format, e.g. 3GPP-H.263-AMR_NB.
     *
     * <li>Server replies with a video sequence number to be used in response header
     * <code>Moteve-Sequence</code> and the response body.</li>
     *
     * <li>Client is then sending the video parts in the POST data. Each request must contain
     * in its <code>Moteve-Sequence</code> header the obtained number.
     * The video parts are added to the video sequence in the order as they are received.
     *
     * <li>When all video parts has been uploaded, the client close the process by sending
     * the <code>Moteve-Sequence</code> header set to <code>close_<the_seq_number></code>.
     *
     * Header <code>Moteve-Token</code> must be set for all operations.</li>
     * </ul>
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/mca/upload.htm", method = RequestMethod.POST)
    public void uploadVideo(HttpServletRequest request, HttpServletResponse response) {
        if (logger.isDebugEnabled()) {
            printHeaders(request);
        }

        PrintWriter out = null;

        try {
            out = new PrintWriter(response.getOutputStream());
            response.setContentType("text/html");

            String sequence = request.getHeader("Moteve-Sequence");
            logger.debug("sequence=" + sequence);
            if (sequence == null || sequence.length() == 0) {
                logger.debug("Missing Moteve-Sequence parameter");
                response.setHeader("Moteve-Sequence", "ERROR: Missing Moteve-Sequence parameter");
                out.print("ERROR: Missing Moteve-Sequence parameter");
                return;
            }

            User user = getUserFromToken(request);
            if (user == null) {
                logger.debug("Token error");
                response.setHeader("Moteve-Token", "ERROR: Token error");
                out.print("ERROR: Token error");
                return;
            }

            if (sequence.equals("new")) {
                String defaultGroupName = request.getHeader("Moteve-DefaultGroup");
                String mediaFormat = request.getHeader("Moteve-MediaFormat");
                try {
                    logger.debug("invoking videoService.startRecording(" + user + ", " + mediaFormat + ", " + defaultGroupName + ")");
                    Video video = videoService.startRecording(user, mediaFormat, defaultGroupName);
                    logger.info("Started a new video, id=" + video.getId() + ", author=" + video.getAuthor().getEmail());
                    response.setHeader("Moteve-Sequence", String.valueOf(video.getId()));
                    out.print(String.valueOf(video.getId()));
                } catch (MoteveException e) {
                    response.setHeader("Moteve-Sequence", "ERROR: " + e.getMessage());
                    out.print("ERROR: " + e.getMessage());
                }

            } else if (sequence.startsWith("close_")) {
                sequence = sequence.substring("close_".length());
                try {
                    Long videoId = Long.parseLong(sequence);
                    videoService.finishRecording(user, videoId);
                    logger.info("Closed video, id=" + videoId);
                    response.setHeader("Moteve-Sequence", sequence + " closed");
                    out.print(sequence + " closed");
                } catch (NumberFormatException e) {
                    logger.debug("Wrong sequence number format");
                    response.setHeader("Moteve-Sequence", "ERROR: Wrong sequence number format");
                    out.print("ERROR: Wrong sequence number format");
                } catch (MoteveException e) {
                    logger.error(e);
                    response.setHeader("Moteve-Sequence", "ERROR: " + e.getMessage());
                    out.print("ERROR: " + e.getMessage());
                }

            } else {
                try {
                    Long videoId = Long.parseLong(sequence);
                    videoService.addPart(videoId, request.getInputStream());
                    logger.info("Added a video part, videoId=" + videoId);
                    response.setHeader("Moteve-Sequence", "OK");
                    out.print("OK");
                } catch (NumberFormatException e) {
                    logger.debug("Wrong sequence number format");
                    response.setHeader("Moteve-Sequence", "ERROR: Wrong sequence number format");
                    out.print("ERROR: Wrong sequence number format");
                } catch (Exception e) {
                    logger.error(e);
                    response.setHeader("Moteve-Sequence", "ERROR: " + e.getMessage());
                    out.print("ERROR: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("Error processing video upload request", e);
            out.print("ERROR: System error");
        } finally {
            out.flush();
            out.close();
        }
    }

    private User getUserFromToken(HttpServletRequest request) {
        String token = request.getHeader("Moteve-Token");
        if (token == null) {
            return null;
        }

        User user = userService.getUserForDevice(token);
        logger.debug("token " + token + " belongs to user " + user.getEmail());
        if (user == null) {
            return null;
        }
        return user;
    }

    private void printHeaders(HttpServletRequest request) {
        for (Enumeration headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String headerName = (String) headerNames.nextElement();
            logger.debug("Request header '" + headerName + "'='" + request.getHeader(headerName) + "'");
        }
    }
}
