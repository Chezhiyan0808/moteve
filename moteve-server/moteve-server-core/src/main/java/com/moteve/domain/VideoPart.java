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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * A piece of a a <code>Video</code>.
 *
 * @author Radek Skokan
 */
@Entity
@Table(name = "video_part")
public class VideoPart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "capture_time", nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date captureTime;

    @Column(name = "conversion_start")
    private Timestamp conversionStart;

    @Column(name = "conversion_end")
    private Timestamp conversionEnd;

    @Column(name = "source_location")
    private String sourceLocation;

    @Column(name = "target_location")
    private String targetLocation;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private Video video;

    @OneToOne
    private VideoPart nextPart;

    @Column(name = "transcoding_failed", nullable = false)
    private boolean transcodingFailed;

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public Timestamp getConversionEnd() {
        return conversionEnd;
    }

    public void setConversionEnd(Timestamp conversionEnd) {
        this.conversionEnd = conversionEnd;
    }

    public Timestamp getConversionStart() {
        return conversionStart;
    }

    public void setConversionStart(Timestamp conversionStart) {
        this.conversionStart = conversionStart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public VideoPart getNextPart() {
        return nextPart;
    }

    public void setNextPart(VideoPart nextPart) {
        this.nextPart = nextPart;
    }

    public boolean isTranscodingFailed() {
        return transcodingFailed;
    }

    public void setTranscodingFailed(boolean transcodingFailed) {
        this.transcodingFailed = transcodingFailed;
    }

    @Override
    public String toString() {
        return "(VideoPart ID=" + id + ")";
    }
}
