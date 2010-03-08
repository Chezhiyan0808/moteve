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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Objects of this class represent mobile devices that users have authenticated
 * (registered) with Moteve server. Each user can have many devices that videos
 * are streamed up from.
 *
 * @author Radek Skokan
 */
@Entity
@Table(name = "device", uniqueConstraints=@UniqueConstraint(columnNames={"token"}))
public class Device implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name="description")
    private String description;

    /**
     * Security token used for communication with MCAs.
     * The token must be unique so it is possible to identify the device and user.
     */
    @Column(name="token", unique=true, nullable=false)
    private String token;

    @Column(name="auth_date", nullable=false)
    @Temporal(TemporalType.DATE)
    Date authDate;

    /**
     * The user that the device is associated to
     */
    @ManyToOne(optional=false)
    private User user;

    public Date getAuthDate() {
        return authDate;
    }

    public void setAuthDate(Date authDate) {
        this.authDate = authDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "(Device ID=" + id + ", description=" + description + ")";
    }

}
