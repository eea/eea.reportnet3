package org.eea.orchestrator.persistence.repository;

import org.apache.commons.lang3.StringUtils;
import org.eea.orchestrator.persistence.domain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobExtendedRepositoryImpl implements JobExtendedRepository{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobExtendedRepository.class);


    private static final String JOBS_QUERY = "select * from jobs ";

    private static final String COUNT_JOBS_QUERY = "select count(*) from jobs";

    /** The entity manager. */
    @PersistenceContext(name = "orchestratorEntityManagerFactory")
    private EntityManager entityManager;

    /**
     * Retrieves paginated jobs
     */
    @Override
    public List<Job> findJobsPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, String providerName, Long datasetId, String datasetName,
                                       String creatorUsername, String jobStatuses, String preparationCode){

        StringBuilder stringQuery = new StringBuilder();
        List<Job> jobList = new ArrayList<>();
        Query query = constructQuery(asc, sortedColumn, stringQuery, false, pageable, jobId, jobTypes, dataflowId, dataflowName, providerId, providerName, datasetId, datasetName, creatorUsername, jobStatuses, preparationCode);

        try {
            jobList = (List<Job>) query.getResultList();
        } catch (NoResultException e) {
            LOG.info(String.format(
                    "No processes found with provided filters: obId = %s, jobType = %s, dataflowId = %s, dataflowName = %s, providerId = %s, providerName = %s, datasetId = %s, datasetName = %s, creatorUsername = %s, jobStatus = %s. Error message: %s",
                    jobId, jobTypes, dataflowId, dataflowName, providerId, providerName, datasetId, datasetName, creatorUsername, jobStatuses, e.getMessage()));
        }
        return jobList;
    }

    /**
     * Count jobs paginated.
     */
    @Override
    public Long countJobsPaginated(boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses) {
        StringBuilder stringQuery = new StringBuilder();
        Query query = constructQuery(asc, sortedColumn, stringQuery, true, null, jobId, jobTypes, dataflowId, dataflowName, providerId, providerName, datasetId, datasetName, creatorUsername, jobStatuses, null);

        return Long.valueOf(query.getSingleResult().toString());
    }

    /**
     * Construct query.
     * @param asc
     * @param sortedColumn
     * @param stringQuery
     * @param countQuery
     * @param pageable
     * @param jobId
     * @param jobTypes
     * @param dataflowId
     * @param dataflowName
     * @param providerId
     * @param providerName
     * @param datasetId
     * @param datasetName
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    private Query constructQuery(boolean asc, String sortedColumn, StringBuilder stringQuery, boolean countQuery, Pageable pageable, Long jobId, String jobTypes, Long dataflowId, String dataflowName,
                                 Long providerId, String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses, String preparationCode) {
        stringQuery.append(countQuery ? COUNT_JOBS_QUERY : JOBS_QUERY);
        addFilters(stringQuery, jobId, jobTypes, dataflowId, dataflowName, providerId, providerName, datasetId, datasetName, creatorUsername, jobStatuses, preparationCode);
        if (!countQuery) {
            stringQuery.append(" order by " + sortedColumn);
            stringQuery.append(asc ? " asc" : " desc");
            if (sortedColumn.equals("job_type")) {
                stringQuery.append(", release");
                stringQuery.append(" desc");
            }
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
            query = entityManager.createNativeQuery(stringQuery.toString(), Job.class);
        }

        addParameters(query, jobId, jobTypes, dataflowId, dataflowName, providerId, providerName, datasetId, datasetName, creatorUsername, jobStatuses, preparationCode);
        return query;
    }

    /**
     * Adds the filters.
     *
     * @param query the query
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param dataflowId the dataflowId
     * @param dataflowName the dataflowName
     * @param providerId the providerId
     * @param providerName the providerName
     * @param datasetId the datasetId
     * @param datasetName the datasetName
     * @param creatorUsername the creatorUsername
     * @param jobStatuses the jobStatuses
     */
    private void addFilters(StringBuilder query, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses, String preparationCode) {
        if (StringUtils.isNotBlank(providerName) && providerId != null && providerId > 0) {
            query.append(" left join data_provider on data_provider.id = jobs.provider_id ");
        }
        query.append(" where 1=1 ");
        query.append((jobId != null) ? " and jobs.id = :jobId " : "");
        query.append(StringUtils.isNotBlank(jobTypes) ? " and jobs.job_type in :jobType " : "");
        query.append((dataflowId != null) ? " and jobs.dataflow_id= :dataflowId " : "");
        query.append(StringUtils.isNotBlank(dataflowName) ? " and LOWER(jobs.dataflow_name) LIKE LOWER(CONCAT('%',:dataflowName,'%')) " : "");
        query.append((providerId != null) ? " and jobs.provider_id= :providerId " : "");
        query.append((StringUtils.isNotBlank(providerName)  && providerId != null && providerId > 0)? " and LOWER(data_provider.label) LIKE LOWER(CONCAT('%',:providerName,'%')) " : "");
        query.append((datasetId != null) ? " and jobs.dataset_id= :datasetId " : "");
        query.append(StringUtils.isNotBlank(datasetName) ? " and LOWER(jobs.dataset_name) LIKE LOWER(CONCAT('%',:datasetName,'%')) " : "");
        query.append(StringUtils.isNotBlank(creatorUsername) ? " and LOWER(jobs.creator_username) LIKE LOWER(CONCAT('%',:creatorUsername,'%')) " : "");
        query.append(StringUtils.isNotBlank(preparationCode) ? " and jobs.preparation_code= :preparationCode " : "");
        query.append(StringUtils.isNotBlank(jobStatuses) ? " and jobs.job_status in :jobStatus " : "");
    }

    /**
     * Adds the parameters.
     *
     * @param query the query
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param dataflowId the dataflowId
     * @param dataflowName the dataflowName
     * @param providerId the providerId
     * @param providerName the providerName
     * @param datasetId the datasetId
     * @param datasetName the datasetName
     * @param creatorUsername the creatorUsername
     * @param jobStatuses the jobStatuses
     */
    private void addParameters(Query query, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses, String preparationCode) {
        if (jobId != null){
            query.setParameter("jobId", jobId);
        }
        if (StringUtils.isNotBlank(jobTypes)){
            query.setParameter("jobType", Arrays.asList(jobTypes.split(",")));
        }
        if (dataflowId != null){
            query.setParameter("dataflowId", dataflowId);
        }
        if (StringUtils.isNotBlank(dataflowName)) {
            query.setParameter("dataflowName", dataflowName);
        }
        if (providerId != null){
            query.setParameter("providerId", providerId);
        }
        if (StringUtils.isNotBlank(providerName)) {
            query.setParameter("providerName", providerName);
        }
        if (datasetId != null){
            query.setParameter("datasetId", datasetId);
        }
        if (StringUtils.isNotBlank(datasetName)) {
            query.setParameter("datasetName", datasetName);
        }
        if (StringUtils.isNotBlank(creatorUsername)) {
            query.setParameter("creatorUsername", creatorUsername);
        }
        if (StringUtils.isNotBlank(preparationCode)) {
            query.setParameter("preparationCode", preparationCode);
        }
        if (StringUtils.isNotBlank(jobStatuses)){
            query.setParameter("jobStatus", Arrays.asList(jobStatuses.split(",")));
        }
    }

    /**
     * Save and commit changes to db
     * @param job
     * @return the saved job
     */
    @Override
    @Transactional
    public Job saveAndFlushJobManually(Job job){
        Job savedJob;
        if (job.getId() == null) {
            entityManager.persist(job);
            savedJob = job;
        } else {
            savedJob = entityManager.merge(job);
        }
        entityManager.flush();
        return savedJob;
    }
}
