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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * A user has contacts to another users (only physical users, no groups/roles); is linked with them. M:N.
 * To help the user to categorize his contacts and perform bulk operations like setting access rights to a video, user can add a subset of his contacts to a group.
 * One of the user's contacts can be in more user's group. (Fred is in Family and in Prague group in the same time).
 * A group can contain users from the contact list and also another groups of the user (group CZ contains Prague and Pilsen). This is the reason for the Role.
 * Video persmissions are specified to role. It covers groups and users.
 *
 * @author Radek Skokan
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="mt_role")
//@DiscriminatorColumn(name="role_type")
public abstract class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
