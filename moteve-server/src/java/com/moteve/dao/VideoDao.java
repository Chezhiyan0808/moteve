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
import com.moteve.domain.Role;
import com.moteve.domain.User;
import com.moteve.domain.Video;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Radek Skokan
 */
@Repository
public class VideoDao {

    private static final Logger logger = Logger.getLogger(VideoDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Video store(Video video) {
        return entityManager.merge(video);
    }

    @Transactional
    public void delete(Long videoId) {
        Video video = entityManager.find(Video.class, videoId);
        entityManager.remove(video);
    }

    @Transactional(readOnly = true)
    public Video findById(Long videoId) {
        return entityManager.find(Video.class, videoId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Video> findAll() {
        Query query = entityManager.createQuery("SELECT v FROM Video v");
        return query.getResultList();
    }

    /**
     * Returns recent <code>count</count> videos that have PUBLIC
     * access permissions.
     * @param count limits the number of returned videos
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Video> findRecentPublic(int count) {
        logger.debug("Getting recent " + count + " videos accessible for ANONYMOUS");
        Query query = entityManager.createQuery("SELECT v FROM Video v, IN (v.permissions) p "
                + "WHERE p.name = '" + Group.PUBLIC + "' ORDER BY v.creationDate DESC");
        query.setMaxResults(count);
        return query.getResultList();
    }

    /**
     * Returns recent <code>count</count> videos that have PUBLIC
     * access permissions or the user has permissions for them.
     * That means he is either
     * <ul>
     * <li>the author of the video</li>
     * <li>associated directly with the video permissions</li>
     * <li>member of one of the groups associated to the video permissions;
     *      the groups can be nested in several levels</li>
     * </ul>
     * @param count limits the number of returned videos
     * @param email identifies the user
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Video> findRecent(int count, String email) {
        logger.debug("Getting recent " + count + " videos for accessible for " + email);
        String userGroups = findGroupsForUser(email);
        String queryString;
        if (userGroups == null || userGroups.length() == 0) {
                queryString = "SELECT v FROM Video v, IN (v.permissions) p "
                + "WHERE p.name = '" + Group.PUBLIC + "' "
                + "OR v.author.email = :email "
                + "OR p.email = :email "
                + "ORDER BY v.creationDate DESC";
        } else {
            queryString = "SELECT v FROM Video v, IN (v.permissions) p "
                + "WHERE p.name = '" + Group.PUBLIC + "' "
                + "OR v.author.email = :email "
                + "OR p.email = :email "
                + "OR p.name IN (" + userGroups + ") "
                + "ORDER BY v.creationDate DESC";
        }
        logger.debug("findRecent(" + count + ", " + email + ") queryString: " + queryString);
        Query query = entityManager.createQuery(queryString);
        query.setParameter("email", email);
        query.setMaxResults(count);
        return query.getResultList();
    }

    private String findGroupsForUser(String email) {
        StringBuilder groupNames = new StringBuilder();
        List<Group> groups = null;
        // get groups that directly contain the user
        try {
            Query query = entityManager.createQuery("SELECT g FROM Group g, IN (g.members) m WHERE m.email = :email");
            query.setParameter("email", email);
            groups = query.getResultList();
            for (Group group : groups) {
                groupNames.append("'" + group.getName() + "'").append(", ");
            }
        } catch (NoResultException e) {
            // user is not a member of any group
        }
        logger.debug("User " + email + " is a direct member of groups: [" + groupNames + "]");
        if (groups == null || groups.size() == 0) {
            logger.debug("User " + email + " is not a member of any group");
            return "";
        }

        // get parent groups
        StringBuilder searchInGroups = new StringBuilder(groupNames);
        try {
            while (searchInGroups.length() > 0) {
                searchInGroups.setLength(groupNames.length() - ", ".length()); // remove the tailing ", "
                String queryString = "SELECT g FROM Group g, IN (g.members) m WHERE m.name IN (" + searchInGroups.toString() + ")";
                logger.debug("Searching for parent groups of groups [" + searchInGroups + "]: " + queryString);
                Query query = entityManager.createQuery(queryString);
                groups = query.getResultList();
                searchInGroups.setLength(0);
                for (Group group : groups) {
                    searchInGroups.append("'" + group.getName() + "'").append(", ");
                    groupNames.append("'" + group.getName() + "'").append(", ");
                }
                logger.debug("Parent groups found: [" + searchInGroups + "]");
            }
        } catch (NoResultException e) {
            // ok, we reached the end of the group hierarchy, there are no more parent groups
        }

        groupNames.setLength(groupNames.length() - ", ".length()); // remove the tailing ", "
        logger.debug("Group search finished. User " + email + " is a member of: [" + groupNames + "]");
        return groupNames.toString();
    }
}
