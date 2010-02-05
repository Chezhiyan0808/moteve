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

import com.moteve.domain.User;
import com.moteve.service.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Radek Skokan
 */
@Controller
public class ContactController {

    private static final Logger logger = Logger.getLogger(ContactController.class);
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/contact/addContact.htm", method = RequestMethod.GET)
    public String setupForm(@ModelAttribute("searchCriteria") String searchCriteria) {
        return "contact/addContact";
    }

    @RequestMapping(value = "/contact/addContact.htm", method = RequestMethod.POST)
    public ModelAndView addContact(HttpServletRequest request,
            @RequestParam(required = false, value = "selectedUsers") List<Long> selectedUsers) {
        if (selectedUsers != null) {
            logger.info("selected " + selectedUsers.size() + " users");
            userService.addContacts(request.getRemoteUser(), selectedUsers);
        }
        return null;
    }

    @RequestMapping(value = "/contact/searchUsers.htm", method = RequestMethod.POST)
    public ModelAndView searchUsers(@RequestParam("searchCriteria") String searchCriteria) {
        return new ModelAndView("contact/addContact", "users", userService.findUsers(searchCriteria));
    }

    @RequestMapping(value = "/contact/listContacts.htm", method = RequestMethod.GET)
    public ModelAndView listContacts(HttpServletRequest request) {
        Set<User> contacts;
        String email = request.getRemoteUser();
        if (email == null) {
            contacts = new HashSet<User>();
        } else {
            User user = userService.findUserByEmail(email);
            contacts = user.getContacts();
        }
        return new ModelAndView("contact/listContacts", "users", contacts);
    }
}
