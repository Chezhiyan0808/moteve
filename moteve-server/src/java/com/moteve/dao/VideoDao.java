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
import com.moteve.domain.User;
import com.moteve.domain.Video;
import com.moteve.domain.VideoSearchCriteria;
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

    /**
     * Returns recent <code>count</count> videos that have PUBLIC
     * access permissions.
     * Does not include videos removed or marked for removal.
     * @param count limits the number of returned videos
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Video> findRecentPublic(int count) {
        logger.debug("Getting recent " + count + " videos accessible for ANONYMOUS");
        Query query = entityManager.createQuery("SELECT DISTINCT v FROM Video v, IN (v.permissions) p "
                + "WHERE p.name = '" + Group.PUBLIC + "' "
                + "AND v.markedForRemoval <> TRUE AND v.removed <> TRUE ORDER BY v.creationDate DESC");
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
     * Does not include videos removed or marked for removal.
     * @param count limits the number of returned videos
     * @param email identifies the user
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Video> findRecent(int count, String email) {
        logger.debug("Getting recent " + count + " videos accessible for " + email);
        String queryString = "SELECT DISTINCT v FROM Video v, IN (v.permissions) p "
                + "WHERE " + buildVideoRestrictionClause(email) + " "
                + "AND v.markedForRemoval <> TRUE AND v.removed <> TRUE ORDER BY v.creationDate DESC";
        logger.debug("findRecent(" + count + ", " + email + ") query: " + queryString);
        Query query = entityManager.createQuery(queryString);
        query.setMaxResults(count);
        return query.getResultList();
    }

    /**
     * Builds the WHERE part of the SELECT command that restricts selected videos
     * to those the user has permissions for.
     * @param email identifies the user; if null, only PUBLIC videos are returned
     * @return the WHERE part of the SELECT clause, without the WHERE keyword; only the condition itself
     */
    private String buildVideoRestrictionClause(String email) {
        String clause;

        if (email == null) {
            clause = "(p.name = '" + Group.PUBLIC + "') ";
        } else {

            String userGroups = findGroupsForUser(email);
            if (userGroups == null || userGroups.length() == 0) {
                clause = "(p.name = '" + Group.PUBLIC + "' "
                        + "OR v.author.email = '" + email + "' "
                        + "OR p.email = '" + email + "') ";
            } else {
                clause = "(p.name = '" + Group.PUBLIC + "' "
                        + "OR v.author.email = '" + email + "' "
                        + "OR p.email = '" + email + "' "
                        + "OR p.name IN (" + userGroups + ")) ";
            }
        }
        logger.debug("Built video permissions-restriction clause: " + clause);
        return clause;
    }

    /**
     * Return groups that the user is member of.
     * @param email
     * @return group names separated with ", " that the user is member of
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    private String findGroupsForUser(String email) {
        // TODO: slow. Use e.g. a stored procedure or when the membership and
        // group hierarchy changes, update a dedicated DB fields
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

    /**
     * Finds all videos matching the criteria. Returned are only the videos that
     * the calling user has permissions for.
     * Does not include videos removed or marked for removal.
     * @param email identifies the user calling this operation; if null, only PUBLIC videos are returned
     * @param criteria the video search criteria
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Video> findByCriteria(String email, VideoSearchCriteria criteria) {
        StringBuilder queryString = new StringBuilder();

        // build the criteria-part query
        queryString.append("SELECT DISTINCT v FROM Video v, IN (v.permissions) p "
                + "WHERE v.markedForRemoval <> TRUE AND v.removed <> TRUE AND ");
        if (criteria.getAuthorEmail() != null && criteria.getAuthorEmail().length() > 0) {
            queryString.append("v.author.email = '" + criteria.getAuthorEmail() + "' AND ");
        } else if (criteria.getAuthorPattern() != null && criteria.getAuthorPattern().length() > 0) {
            // if the exact author's email is specified, this pattern is ignored
            queryString.append("(UPPER(v.author.email) LIKE '%" + criteria.getAuthorPattern().toUpperCase() + "%' "
                    + "OR UPPER(v.author.displayName) LIKE '%" + criteria.getAuthorPattern().toUpperCase() + "%') AND ");
        }
        if (criteria.getVideoNamePattern() != null && criteria.getVideoNamePattern().length() > 0) {
            queryString.append("UPPER(v.name) LIKE '%" + criteria.getVideoNamePattern().toUpperCase() + "%' AND ");
        }
        if (criteria.getDateFrom() != null) {
            queryString.append("v.creationDate >= :dateFrom AND ");
        }
        if (criteria.getDateTo() != null) {
            queryString.append("v.creationDate <= :dateTo AND ");
        }
        if (criteria.isLive()) {
            queryString.append("v.recordInProgress = TRUE AND ");
        }
        logger.debug("Video search criteria, criteria query part: " + queryString.toString());

        // add the permissions-restriction query part
        queryString.append(buildVideoRestrictionClause(email));
        logger.debug("findByCriteria query: " + queryString);

        Query query = entityManager.createQuery(queryString.toString());
        if (criteria.getDateFrom() != null) {
            query.setParameter("dateFrom", criteria.getDateFrom());
        }
        if (criteria.getDateTo() != null) {
            query.setParameter("dateTo", criteria.getDateTo());
        }
        return query.getResultList();
    }

    /**
     * Finds permission contacts available for the given user and video. The result are
     * contacts that the user has and are not already associated with the specified video.
     * @param email idenifies the user
     * @param videoId identifies the video
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findAvailableVideoContacts(String email, Long videoId) {
        Query query = entityManager.createQuery("SELECT c FROM User u, IN (u.contacts) c "
                + "WHERE u.email = :email AND NOT EXISTS "
                + "(SELECT p FROM Video v, IN (v.permissions) p WHERE v.id = :videoId AND c.id = p.id)");
        query.setParameter("email", email);
        query.setParameter("videoId", videoId);
        return query.getResultList();
    }

    /**
     * Finds contacts associated with the specified video.
     * @param videoId
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<User> findVideoContacts(Long videoId) {
        Query query = entityManager.createQuery("SELECT p FROM Video v, IN (v.permissions) p "
                + "WHERE v.id = :videoId AND p.email IS NOT NULL");
        query.setParameter("videoId", videoId);
        return query.getResultList();
    }

    /**
     * Finds permission groups available for the given user and video. The result are
     * groups that the user has and are not already associated with the specified video.
     * @param email idenifies the user
     * @param videoId identifies the video
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Group> findAvailableVideoGroups(String email, Long videoId) {
        Query query = entityManager.createQuery("SELECT g FROM User u, IN (u.groups) g "
                + "WHERE u.email = :email AND NOT EXISTS "
                + "(SELECT p FROM Video v, IN (v.permissions) p WHERE v.id = :videoId AND g.id = p.id)");
        query.setParameter("email", email);
        query.setParameter("videoId", videoId);
        return query.getResultList();
    }

    /**
     * Finds the video athor's groups associated with the specified video.
     * @param videoId
     * @return
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Group> findVideoGroups(Long videoId) {
        Query query = entityManager.createQuery("SELECT p FROM Video v, IN (v.permissions) p WHERE v.id = :videoId AND p.email IS NULL");
        query.setParameter("videoId", videoId);
        return query.getResultList();
    }
}
