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

import com.moteve.domain.Device;
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
public class DeviceDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Device store(Device device) {
        return entityManager.merge(device);
    }

    @Transactional
    public void delete(Long deviceId) {
        Device device = entityManager.find(Device.class, deviceId);
        entityManager.remove(device);
    }

    @Transactional(readOnly = true)
    public Device findById(Long deviceId) {
        return entityManager.find(Device.class, deviceId);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Device> findAll() {
        Query query = entityManager.createQuery("SELECT d FROM Device d");
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Device findByToken(String token) {
        Query query = entityManager.createQuery("SELECT D FROM Device d WHERE d.token = :token");
        query.setParameter("token", token);
        return (Device) query.getSingleResult();
    }
}
