package org.eea.collaboration.axon.config;

import org.axonframework.common.jpa.EntityManagerProvider;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AxonEntityManagerProvider implements EntityManagerProvider {

    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext(unitName = "eventStorePersistenceUnit")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
