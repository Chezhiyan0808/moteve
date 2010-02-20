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
import com.moteve.domain.User;
import com.moteve.service.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Radek Skokan
 */
@Controller
@SessionAttributes("group")
public class GroupController {

    private static final Logger logger = Logger.getLogger(GroupController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/group/manageGroups.htm", method = RequestMethod.GET)
    public ModelAndView displayGroups(HttpServletRequest request) {
        return new ModelAndView("group/manageGroups", "groups", userService.getGroups(request.getRemoteUser()));
    }

    @RequestMapping(value = "/group/createGroup.htm", method = RequestMethod.POST)
    public String createGroup(HttpServletRequest request,
            @RequestParam(required = false, value = "groupName") String groupName) {
        if (groupName != null && groupName.length() > 0) {
            // TODO: add validation if the group already exists for the user
            userService.createGroup(request.getRemoteUser(), groupName);
        }
        return "redirect:/group/manageGroups.htm";
    }

    @RequestMapping(value = "/group/removeGroups.htm", method = RequestMethod.POST)
    public String removeGroups(HttpServletRequest request,
            @RequestParam(required = false, value = "selectedGroups") List<Long> selectedGroups) {
        if (selectedGroups != null) {
            logger.info("selected " + selectedGroups.size() + " groups for deletion");
            userService.removeGroups(request.getRemoteUser(), selectedGroups);
        }
        return "redirect:/group/manageGroups.htm";
    }

    @RequestMapping(value = "/group/manageGroupMembers.htm", method = RequestMethod.GET)
    public ModelAndView setMembersForm(HttpServletRequest request,
            @RequestParam(value="groupId") Long groupId) {
        Map<String, Object> model = new HashMap<String, Object>();
        Group group = userService.getGroup(groupId);
        model.put("group", group);
        List<User> availableContacts = userService.getAvailableContacts(request.getRemoteUser(), groupId);
        List<User> groupMembers = userService.getGroupMembers(request.getRemoteUser(), groupId);
        model.put("availableContacts", availableContacts);
        model.put("groupMembers", groupMembers);
        return new ModelAndView("/group/manageGroupMembers", model);
    }

    @RequestMapping(value = "/group/setGroupMembers.htm", method = RequestMethod.POST)
    public String setGroupMembers(HttpServletRequest request,
            @ModelAttribute("group") Group group,
            @RequestParam(value="groupMembers", required=false) List<Long> groupMembers) {
        if (groupMembers == null) {
            groupMembers = new ArrayList<Long>();
        }
        userService.setGroupMembers(group.getId(), groupMembers);
        return "redirect:/group/manageGroupMembers.htm?groupId=" + group.getId();
    }
}
