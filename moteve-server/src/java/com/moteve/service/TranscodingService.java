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

import com.moteve.dao.VideoPartDao;
import com.moteve.domain.UnknownMediaFormatException;
import com.moteve.domain.VideoPart;
import java.sql.Timestamp;
import java.util.Map;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Transcodes video part from one format to another.
 * There is a limited number of running transcoding processes. When a process
 * finishes, Transcoder checks if there are availeble any more video parts that are
 * not transcoded yet (conversionStart = null and conversionEnd = null).
 *
 * To avoid the situation no transcoding process is running and there are 
 * video parts to be transcoded, call the work() method when a new video part
 * became available or when the application has been started.
 *
 * @author Radek Skokan
 */
//@Service commented out as it needs some config params, so declared in XML
public class TranscodingService {

    /**
     * The maximum number of running processes transcoding the videos (e.g. FFMPEG)
     */
    private int maxProcesses;

    /**
     * Mapping between media format names and commands to execute the transcoding
     */
    private Map<String, String> formatConvertors;

    @Autowired
    private VideoPartDao videoPartDao;

    private int runningProcesses = 0;

    private static final Logger logger = Logger.getLogger(TranscodingService.class);

    @Required
    public void setMaxProcesses(int maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    @Required
    public void setFormatConvertors(Map<String, String> formatConvertors) {
        this.formatConvertors = formatConvertors;
    }

    /**
     * Notifies the Transcoder there are available video parts to be trascoded.
     */
    public void work() {
        if (runningProcesses < maxProcesses) {
            runningProcesses++;
            logger.debug("Transcode process capacity available; running " + runningProcesses + " process(es); max=" + maxProcesses);
            transcodeNextVideoPart();
        } else {
            logger.debug("No action; running " + runningProcesses + " process(es); max=" + maxProcesses);
        }
    }

    /**
     * Finds and transcodes the oldest available video part that was not yet transcoded
     * (conversionStart = null and conversionEnd = null).
     */
    private void transcodeNextVideoPart() {
        try {
            VideoPart part;
            synchronized (this) {
                // synchronized to prevent transcoding of the same part start more than once
                part = videoPartDao.findNextForTranscoding();
                part.setConversionStart(new Timestamp(System.currentTimeMillis()));
                part = videoPartDao.store(part);
            }

            Thread worker = new Thread(new TranscodingWorker(this, part));
            logger.info("Starting transcoding of video part ID=" + part.getId() + ", src=" + part.getSourceLocation()
                    + " [running " + runningProcesses + " process(es); max=" + maxProcesses + "]");
            worker.start();
        } catch (NoResultException e) {
            logger.debug("No video part is available for transcoding");
        }
    }

    /**
     * MUST always be called by TranscodingWorker when a video part transcoding has finished
     * @param videoPart
     * @param success whether the conversion was successful
     */
    public void transcodingFinished(VideoPart part, boolean success) {
        try {
            if (success) {
                logger.info("Finished transcoding of video part ID=" + part.getId() + ", src=" + part.getSourceLocation());
                part.setConversionStart(new Timestamp(System.currentTimeMillis()));
            } else {
                logger.error("Transcoding of video part ID=" + part.getId() + ", src=" + part.getSourceLocation() + " failed");
                part.setTranscodingFailed(true);
            }
            videoPartDao.store(part);
        } catch (Exception e) {
            // just in case, catch all possible issues
            logger.error(e);
        }

        // try to transcode next available video parts
        runningProcesses--;
        work();
    }

    /**
     * Decides from the media format of the  video associated to the video part
     * what transcoding needs to be applied on the video part.
     * @param part
     * @return command to be executed to transcode the video part
     * @throws UnknownMediaFormatException
     */
    String getCommand(VideoPart part) throws UnknownMediaFormatException {
        String command = formatConvertors.get(part.getVideo().getSourceFormat().getName());
        if (command == null) {
            throw new UnknownMediaFormatException("Cannot transcode video part ID=" + part.getId() + ", src="
                    + part.getSourceLocation() + ". Unknown media format " + part.getVideo().getSourceFormat());
        }

        // replace SOURCE and TARGET in the command
        command = command.replaceAll("SOURCE", part.getSourceLocation());
        command = command.replaceAll("TARGET", part.getTargetLocation());
        return command;
    }
}
