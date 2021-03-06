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
package com.moteve.dao;

import com.moteve.domain.Authority;
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
@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public User store(User user) {
        return entityManager.merge(user);
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

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email");
        query.setParameter("email", email);
        return (User) query.getSingleResult();
    }

    /**
     *
     * @return all users that have the <code>Authority.ADMIN</code> security authority
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findAdmins() {
        Query query = entityManager.createQuery("SELECT u FROM User u JOIN u.authorities a "
                + "WHERE a.name = :admin_auth");
        query.setParameter("admin_auth", Authority.ADMIN);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public User findByEmailAndPassword(String email, String encPassword) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email AND password = :password");
        query.setParameter("email", email);
        query.setParameter("password", encPassword);
        return (User) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findByEmailOrDisplayName(String criteria) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE UPPER(u.email) LIKE :criteria OR UPPER(u.displayName) LIKE :criteria");
        query.setParameter("criteria", "%" + criteria.toUpperCase() + "%");
        return query.getResultList();
    }

    /**
     * Finds enabled users mathing the search criteria in email or display name.
     * @param criteria the search criteria
     * @param excludeEmail if not null, user with the specified email address is excluded from the results
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findEnabledByEmailOrDisplayName(String criteria, String excludeEmail) {
        final String queryStringAll = "SELECT u FROM User u WHERE (u.enabled = TRUE) "
                + "AND (UPPER(u.email) LIKE :criteria OR UPPER(u.displayName) LIKE :criteria)";
        final String queryStringExclude = "SELECT u FROM User u WHERE (u.enabled = TRUE) "
                + "AND (UPPER(u.email) LIKE :criteria OR UPPER(u.displayName) LIKE :criteria) "
                + "AND (u.email <> :excludeEmail)";
        Query query;
        if (excludeEmail == null) {
            query = entityManager.createQuery(queryStringAll);
        } else {
            query = entityManager.createQuery(queryStringExclude);
            query.setParameter("excludeEmail", excludeEmail);
        }
        query.setParameter("criteria", "%" + criteria.toUpperCase() + "%");
        return query.getResultList();
    }

    /**
     * Finds contacts available for the given user and group. The result are
     * contacts that the user has and are not already members of the specified group.
     * @param email idenifies the user
     * @param groupId identifies the group
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findAvailableGroupContacts(String email, Long groupId) {
        Query query = entityManager.createQuery("SELECT c FROM User u, IN (u.contacts) c "
                + "WHERE u.email = :email AND NOT EXISTS "
                + "(SELECT m FROM Group g, IN (g.members) m WHERE g.id = :groupId AND c.id = m.id)");
        query.setParameter("email", email);
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }

    /**
     * Finds contacts that the user has in the specified group.
     * @param groupId
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findGroupContacts(Long groupId) {
        //Query query = entityManager.createQuery("SELECT m FROM User u, IN (u.groups) g, IN (g.members) m WHERE u.email = :email AND g.id = :groupId");
        Query query = entityManager.createQuery("SELECT m FROM Group g, IN (g.members) m WHERE g.id = :groupId");
//        query.setParameter("email", email);
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }
}
