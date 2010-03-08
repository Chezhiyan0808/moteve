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
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A group can contain users from the contact list and also another groups of the user (group CZ contains Prague and Pilsen). This is the reason for the Role.
 * 
 * @author Radek Skokan
 */
@Entity
@Table(name="mt_group", uniqueConstraints=@UniqueConstraint(columnNames={"name", "user_id"}))
public class Group extends Role implements Serializable {

    public static final String PUBLIC = "PUBLIC";
    public static final String JUST_ME = "JUST_ME";

    @Column(name = "name", nullable=false) // not unique as many users can have the same name of their groups, which are globally different
    private String name;

    @ManyToMany
    @JoinTable(
        name="group_member",
        joinColumns=@JoinColumn(name="group_id", referencedColumnName="id"),
        inverseJoinColumns=@JoinColumn(name="member_id", referencedColumnName="id"))
    private Set<Role> members;

    @ManyToOne(optional=false)
    private User user;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getMembers() {
        return members;
    }

    public void setMembers(Set<Role> members) {
        this.members = members;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "(Group ID=" + getId() + ", name=" + name + ")";
    }

}
