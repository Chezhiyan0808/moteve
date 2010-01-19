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
import com.moteve.domain.Authority;
import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A utility class that performs initial system setup such as creating required
 * user authorities in the database.
 *
 * @author Radek Skokan
 */
@Service
public class SystemInitializer {

    @Autowired
    private AuthorityDao authorityDao;

    private static final Logger logger = Logger.getLogger(SystemInitializer.class);

    @PostConstruct
    public void init() {
        logger.info("Initializing Moteve system");

        try {
            authorityDao.findByName(Authority.ADMIN);
        } catch (NoResultException e) {
            Authority authAdmin = new Authority();
            authAdmin.setName(Authority.ADMIN);
            authorityDao.store(authAdmin);
        }

        try {
            authorityDao.findByName(Authority.MEMBER);
        } catch (NoResultException e) {
            Authority authMember = new Authority();
            authMember.setName(Authority.MEMBER);
            authorityDao.store(authMember);
        }
    }

}
