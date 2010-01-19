/*
 * Copyright 2009-2010 the original author or authors.
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

import com.moteve.dao.AuthorityDao;
import com.moteve.dao.UserDao;
import com.moteve.domain.Authority;
import com.moteve.domain.User;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Radek Skokan
 */
@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthorityDao authorityDao;

    /**
     * Registers a new user into the system and grants him the MEMBER role.
     * 
     * @param user
     */
    public void register(User user) {
        logger.info("Registering user " + user.getEmail());
        user.setRegistrationDate(new Date());
        Authority memberAuthority = authorityDao.findByName(Authority.MEMBER);
        Set<Authority> authorities = new HashSet<Authority>();
        authorities.add(memberAuthority);
        user.setAuthorities(authorities);
        userDao.store(user);
    }

}
