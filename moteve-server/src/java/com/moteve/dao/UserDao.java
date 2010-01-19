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

import com.moteve.domain.User;
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
@Repository("userDao")
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void store(User user) {
        entityManager.merge(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = entityManager.find(User.class, userId);
        entityManager.remove(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return entityManager.find(User.class, userId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        Query query = entityManager.createQuery("SELECT u FROM User u");
        return query.getResultList();
    }

    @Transactional(readOnly=true)
    public User findByEmail(String email) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email");
        query.setParameter("email", email);
        return  (User) query.getSingleResult();
    }
}
