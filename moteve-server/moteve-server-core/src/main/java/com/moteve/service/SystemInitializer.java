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
package com.moteve.service;

import com.moteve.dao.AuthorityDao;
import com.moteve.dao.UserDao;
import com.moteve.domain.Authority;
import com.moteve.domain.User;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A utility class that performs initial system setup such as creating required
 * database entries.
 *
 * @author Radek Skokan
 */
@Service
public class SystemInitializer {

    @Autowired
    private AuthorityDao authorityDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TranscodingService transcodingService;

    private static final Logger logger = Logger.getLogger(SystemInitializer.class);

    @PostConstruct
    @Transactional
    public void init() {
        logger.info("Initializing Moteve system database");
        checkAuthorities();
        checkAdminUser();
        transcodingService.work();
    }

    private void checkAuthorities() {
        try {
            authorityDao.findByName(Authority.ADMIN);
            logger.info("Authority " + Authority.ADMIN + " exists in DB");
        } catch (NoResultException e) {
            logger.info("Authority " + Authority.ADMIN + " does not exist in DB. Creating");
            Authority authAdmin = new Authority();
            authAdmin.setName(Authority.ADMIN);
            authorityDao.store(authAdmin);
        }

        try {
            authorityDao.findByName(Authority.MEMBER);
            logger.info("Authority " + Authority.MEMBER + " exists in DB");
        } catch (NoResultException e) {
            logger.info("Authority " + Authority.MEMBER + " does not exist in DB. Creating");
            Authority authMember = new Authority();
            authMember.setName(Authority.MEMBER);
            authorityDao.store(authMember);
        }
    }

    private void checkAdminUser() {
        List<User> admins = userDao.findAdmins();
        if (admins.size() < 1) {
            logger.info("No admin user found in DB. Creating 'admin@localhost'");
            User admin = new User();
            admin.setEmail("admin@localhost");
            admin.setPassword("admin");
            admin.setRegistrationDate(new Date());
            admin.setEnabled(true);
            Authority adminAuthority = authorityDao.findByName(Authority.ADMIN);
            Set<Authority> authorities = new HashSet<Authority>();
            authorities.add(adminAuthority);
            admin.setAuthorities(authorities);
            userDao.store(admin);
        } else {
            for (User admin : admins) {
                logger.info("Found admin user: " + admin.getEmail() + " (" + admin.getDisplayName() + ")");
            }
        }
    }
}
