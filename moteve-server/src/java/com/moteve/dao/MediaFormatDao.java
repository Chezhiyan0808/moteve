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

import com.moteve.domain.MediaFormat;
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
public class MediaFormatDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MediaFormat store(MediaFormat mediaFormat) {
        return entityManager.merge(mediaFormat);
    }

    @Transactional
    public void delete(Long mediaFormatId) {
        MediaFormat mediaFormat = entityManager.find(MediaFormat.class, mediaFormatId);
        entityManager.remove(mediaFormat);
    }

    @Transactional(readOnly = true)
    public MediaFormat findById(Long mediaFormatId) {
        return entityManager.find(MediaFormat.class, mediaFormatId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<MediaFormat> findAll() {
        Query query = entityManager.createQuery("SELECT mf FROM MediaFormat mf");
        return query.getResultList();
    }

    public MediaFormat findByName(String name) {
        Query query = entityManager.createQuery("SELECT mf FROM MediaFormat mf WHERE mf.name = :name");
        query.setParameter("name", name);
        return (MediaFormat) query.getSingleResult();
    }
}
