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
package com.moteve.domain;

import java.util.Date;

/**
 *
 * @author Radek Skokan
 */
public class VideoSearchCriteria {

    /**
     * Text contained in the video name.
     */
    private String videoNamePattern;

    /**
     * Text contained in either author's email or display name.
     * Ignored if the authorEmail criteria is specified.
     */
    private String authorPattern;

    /**
     * Eaxact author's email. If specified, the videoNamePattern is ignored.
     */
    private String authorEmail;

    private Date dateFrom;

    private Date dateTo;

    private boolean live;

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorPattern() {
        return authorPattern;
    }

    public void setAuthorPattern(String authorPattern) {
        this.authorPattern = authorPattern;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public String getVideoNamePattern() {
        return videoNamePattern;
    }

    public void setVideoNamePattern(String videoNamePattern) {
        this.videoNamePattern = videoNamePattern;
    }
}
