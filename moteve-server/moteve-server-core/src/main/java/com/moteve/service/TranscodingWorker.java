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

import com.moteve.domain.VideoPart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Radek Skokan
 */
class TranscodingWorker implements Runnable {

    private TranscodingService transcodingService;

    private VideoPart part;

    private static final Logger logger = Logger.getLogger(TranscodingWorker.class);

    public TranscodingWorker(TranscodingService transcodingService, VideoPart part) {
        this.transcodingService = transcodingService;
        this.part = part;
    }

    public void run() {
        boolean success = true;
        InputStreamReader stdInputReader = null;
        InputStreamReader stdErrorReader = null;
        try {
            String command = transcodingService.getCommand(part);
            logger.info("Executing " + command);
            Process p = Runtime.getRuntime().exec(command);
            int c;

            stdInputReader = new InputStreamReader(p.getInputStream());
            StringBuilder stdInput = new StringBuilder();
            while ((c = stdInputReader.read()) != -1) {
                stdInput.append((char) c);
            }

            stdErrorReader = new InputStreamReader(p.getErrorStream());
            StringBuilder stdError = new StringBuilder();
            while ((c = stdErrorReader.read()) != -1) {
                stdError.append((char) c);
            }

            if (stdInput.length() > 0) {
                logger.debug("Transcoder std output: " + stdInput.toString());
            }

            if (stdError.length() > 0) {
                // ffmpeg logs everything to err output
                logger.debug("Transcoder err output: " + stdError.toString());
            }
        } catch (Exception e) {
            success = false;
            logger.error("Error while converting video part ID=" + part.getId() + ", src="
                    + part.getSourceLocation() + ": " + e.getMessage(), e);
        } finally {
            try {
                if (stdInputReader != null) {
                    stdInputReader.close();
                }
                if (stdErrorReader != null) {
                    stdErrorReader.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
            transcodingService.transcodingFinished(part, success);
        }
    }
}
