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
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Radek Skokan
 */
@Entity
@Table(name="mt_user")
public class User extends Role implements Serializable {

    @Column(name="email", unique = true, nullable=false)
    private String email; // also used as login name

    @Column(name="password")
    private String password;

    @Column(name="display_name")
    private String displayName;

    /*
     * User's contact list. Can only be users.
     */
    @ManyToMany
    @JoinTable(
        name="user_contact",
        joinColumns=@JoinColumn(name="user_id", referencedColumnName="id"),
        inverseJoinColumns=@JoinColumn(name="contact_id", referencedColumnName="id"))
    Set<User> contacts;

    @OneToMany(mappedBy="user")
    Set<Group> groups;

    @Column(name="registration_date", nullable=false)
    @Temporal(TemporalType.DATE)
    Date registrationDate;
    
    @Column(name="enabled", nullable=false)
    boolean enabled;

    @ManyToMany
    @JoinTable(
        name="user_authority",
        joinColumns=@JoinColumn(name="user_id", referencedColumnName="id"),
        inverseJoinColumns=@JoinColumn(name="authority_id", referencedColumnName="id"))
    Set<Authority> authorities;

    /**
     * Mobile devices that the user has authenticated
     */
    @OneToMany(mappedBy="user")
    private Set<Device> devices;

    public Set<User> getContacts() {
        return contacts;
    }

    public void setContacts(Set<User> contacts) {
        this.contacts = contacts;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "(User ID=" + getId() + ", email=" + email + ")";
    }

}
