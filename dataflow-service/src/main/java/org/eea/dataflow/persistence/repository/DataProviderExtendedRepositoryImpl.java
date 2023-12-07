package org.eea.dataflow.persistence.repository;

import org.apache.commons.lang3.StringUtils;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class DataProviderExtendedRepositoryImpl implements DataProviderExtendedRepository {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderExtendedRepository.class);


    private static final String PROVIDERS_QUERY = "select * from data_provider ";

    private static final String COUNT_PROVIDERS_QUERY = "select count(*) from data_provider";

    /**
     * The entity manager.
     */
    @PersistenceContext()
    private EntityManager entityManager;

    /**
     * Retrieves paginated providers
     */
    @Override
    public List<DataProvider> findProvidersPaginated(Pageable pageable, boolean asc, String sortedColumn, String providerCode, Integer groupId, String label) {

        StringBuilder stringQuery = new StringBuilder();
        List<DataProvider> providers = new ArrayList<>();
        Query query = constructQuery(asc, sortedColumn, stringQuery, false, pageable, providerCode, groupId, label);

        try {
            providers = (List<DataProvider>) query.getResultList();
        } catch (NoResultException e) {
            LOG.error("No processes found with provided filters: sortedColumn {}, providerCode {}, groupId {}, label {}", sortedColumn, providerCode, groupId, label);
        }
        return providers;
    }

    /**
     * Count providers paginated.
     */
    @Override
    public Long countProvidersPaginated(boolean asc, String sortedColumn, String providerCode, Integer groupId, String label) {
        StringBuilder stringQuery = new StringBuilder();
        Query query = constructQuery(asc, sortedColumn, stringQuery, true, null, providerCode, groupId, label);

        return Long.valueOf(query.getSingleResult().toString());
    }

    /**
     * Construct query.
     * @param asc
     * @param sortedColumn
     * @param stringQuery
     * @param countQuery
     * @param pageable
     * @param providerCode the providerCode
     * @param groupId the groupId
     * @param label the label
     * @return
     */
    private Query constructQuery(boolean asc, String sortedColumn, StringBuilder stringQuery, boolean countQuery, Pageable pageable, String providerCode, Integer groupId, String label) {
        stringQuery.append(countQuery ? COUNT_PROVIDERS_QUERY : PROVIDERS_QUERY );
        addFilters(stringQuery, providerCode, groupId, label);
        if (!countQuery) {
            stringQuery.append(" order by " + sortedColumn);
            stringQuery.append(asc ? " asc" : " desc");
            if (null != pageable) {
                stringQuery.append(" LIMIT " + pageable.getPageSize());
                stringQuery.append(" OFFSET " + pageable.getOffset());
            }
        }
        Query query = null;
        if(countQuery){
            query = entityManager.createNativeQuery(stringQuery.toString());
        }
        else{
            query = entityManager.createNativeQuery(stringQuery.toString(), DataProvider.class);
        }


        addParameters(query, providerCode, groupId, label);
        return query;
    }

    /**
     * Adds the filters.
     *
     * @param query the query
     * @param providerCode the providerCode
     * @param groupId the groupId
     * @param label the label
     */
    private void addFilters(StringBuilder query, String providerCode, Integer groupId, String label) {
        query.append(" where 1=1 ");
        query.append(StringUtils.isNotBlank(providerCode) ? " and LOWER(data_provider.code) like LOWER(CONCAT('%',:providerCode,'%')) " : "");
        query.append((groupId != null) ? " and data_provider.group_id= :groupId " : "");
        query.append(StringUtils.isNotBlank(label) ? " and LOWER(data_provider.label) like LOWER(CONCAT('%',:label,'%')) " : "");
    }

    /**
     * Adds the parameters.
     *
     * @param query the query
     * @param providerCode the providerCode
     * @param groupId the groupId
     * @param label the label
     */
    private void addParameters(Query query, String providerCode, Integer groupId, String label) {
        if(StringUtils.isNotBlank(providerCode)){
            query.setParameter("providerCode", providerCode);
        }
        if(groupId != null){
            query.setParameter("groupId", groupId);
        }
        if (StringUtils.isNotBlank(label)) {
            query.setParameter("label", label);
        }
    }
}