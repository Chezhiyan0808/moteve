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

package com.moteve.dao;

import com.moteve.domain.Authority;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Radek Skokan
 */
@Repository("authorityDao")
public class AuthorityDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void store(Authority authority) {
        entityManager.merge(authority);
    }

    @Transactional
    public void delete(Long authorityId) {
        Authority authority = entityManager.find(Authority.class, authorityId);
        entityManager.remove(authority);
    }

    @Transactional(readOnly = true)
    public Authority findById(Long authorityId) {
        return entityManager.find(Authority.class, authorityId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Authority> findAll() {
        Query query = entityManager.createQuery("SELECT a FROM Authority a");
        return query.getResultList();
    }

    @Transactional(readOnly=true)
    public Authority findByName(String name) {
        Query query = entityManager.createQuery("SELECT a FROM Authority a WHERE a.name = :name");
        query.setParameter("name", name);
        return  (Authority) query.getSingleResult();
    }
}
