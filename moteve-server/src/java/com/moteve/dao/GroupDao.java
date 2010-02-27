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

import com.moteve.domain.Group;
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
public class GroupDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Group store(Group group) {
        return entityManager.merge(group);
    }

    @Transactional
    public void delete(Long groupId) {
        Group group = entityManager.find(Group.class, groupId);
        entityManager.remove(group);
    }

    @Transactional(readOnly = true)
    public Group findById(Long groupId) {
        return entityManager.find(Group.class, groupId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Group> findAll() {
        Query query = entityManager.createQuery("SELECT g FROM Group g");
        return query.getResultList();
    }

    /**
     * Finds the group with given name associated with the user.
     * 
     * @param userId the owner of the group
     * @param allowedGroupName the group name
     * @return
     */
    public Group findByUserAndName(Long userId, String groupName) {
        Query query = entityManager.createQuery("SELECT g FROM Group g, User u " +
                "WHERE u.id = :userId AND g.name = :groupName");
        query.setParameter("userId", userId);
        query.setParameter("groupName", groupName);
        return (Group) query.getSingleResult();
    }
}
