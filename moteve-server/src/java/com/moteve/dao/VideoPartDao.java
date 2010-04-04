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

import com.moteve.domain.VideoPart;
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
public class VideoPartDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public VideoPart store(VideoPart videoPart) {
        return entityManager.merge(videoPart);
    }

    @Transactional
    public void delete(Long videoPartId) {
        VideoPart videoPart = entityManager.find(VideoPart.class, videoPartId);
        entityManager.remove(videoPart);
    }

    @Transactional(readOnly = true)
    public VideoPart findById(Long videoPartId) {
        return entityManager.find(VideoPart.class, videoPartId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<VideoPart> findAll() {
        Query query = entityManager.createQuery("SELECT vp FROM VideoPart vp");
        return query.getResultList();
    }

    /**
     * 
     * @return the oldest available video part that was not yet transcoded
     * (conversionStart = null and conversionEnd = null and transcodingFailed <> true).
     */
    @Transactional(readOnly = true)
    public VideoPart findNextForTranscoding() {
        Query query = entityManager.createQuery("SELECT vp FROM VideoPart vp "
                + "WHERE vp.conversionStart IS NULL AND vp.conversionEnd IS NULL "
                + "AND vp.transcodingFailed <> TRUE "
                + "ORDER BY vp.captureTime DESC");
        query.setMaxResults(1);
        return (VideoPart) query.getSingleResult();
    }
}
