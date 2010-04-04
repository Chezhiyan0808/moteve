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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 *
 * @author Radek Skokan
 */
@Controller
@SessionAttributes("user")
public class RegistrationController {

    private static final Logger logger = Logger.getLogger(RegistrationController.class);

//    @Autowired
//    public RegistrationController(UserService userService, UserValidator validator) {
//    }
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/register.htm", method = RequestMethod.GET)
    public String setupForm(ModelMap model) {
        User user = new User();
        model.addAttribute("user", user);
        return "user/registrationForm";
    }

    @RequestMapping(value = "/register.htm", method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("user") User user, BindingResult result, SessionStatus status) {
        //TODO: validator.validate(user, result);
        // if (result.hasErrors()) { ...

        // userService.register(user);
        status.setComplete();
        userService.register(user);
        return "redirect:registrationSuccess.htm";
    }

    @RequestMapping(value = "/registrationSuccess.htm", method = RequestMethod.GET)
    public String registrationSuccess() {
        return "user/registrationSuccess";
    }
}
