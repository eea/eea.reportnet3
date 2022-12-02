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
    public List<Job> findJobsPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId,
                                       String jobTypes, String creatorUsername, String jobStatuses){

        StringBuilder stringQuery = new StringBuilder();
        List<Job> jobList = new ArrayList<>();
        Query query = constructQuery(asc, sortedColumn, stringQuery, false, pageable, jobId, jobTypes, creatorUsername, jobStatuses);

        try {
            jobList = (List<Job>) query.getResultList();
            LOG.info(String.format(
                    "Retrieved job list with provided filters: jobId = %s, jobType = %s, creatorUsername = %s, jobStatus = %s",
                    jobId, jobTypes, creatorUsername, jobStatuses));
        } catch (NoResultException e) {
            LOG.info(String.format(
                    "No processes found with provided filters: obId = %s, jobType = %s, creatorUsername = %s, jobStatus = %s. Error message: %s",
                    jobId, jobTypes, creatorUsername, jobStatuses, e.getMessage()));
        }
        return jobList;
    }

    /**
     * Count jobs paginated.
     */
    @Override
    public Long countJobsPaginated(boolean asc, String sortedColumn, Long jobId,
                                   String jobTypes, String creatorUsername, String jobStatuses) {
        StringBuilder stringQuery = new StringBuilder();
        Query query = constructQuery(asc, sortedColumn, stringQuery, true, null, jobId, jobTypes, creatorUsername, jobStatuses);

        return Long.valueOf(query.getSingleResult().toString());
    }

    /**
     * Construct query.
     *
     * @param asc the asc
     * @param sortedColumn the filteredColumn
     * @param stringQuery the string query
     * @param pageable the pageable
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param creatorUsername the creatorUsername
     * @param jobStatuses the jobStatuses
     * @return the query
     */
    private Query constructQuery(boolean asc, String sortedColumn, StringBuilder stringQuery, boolean countQuery, Pageable pageable, Long jobId,
                                 String jobTypes, String creatorUsername, String jobStatuses) {
        stringQuery.append(countQuery ? COUNT_JOBS_QUERY : JOBS_QUERY);
        addFilters(stringQuery, jobId, jobTypes, creatorUsername, jobStatuses);
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
            query = entityManager.createNativeQuery(stringQuery.toString(), Job.class);
        }


        addParameters(query, jobId, jobTypes, creatorUsername, jobStatuses);
        return query;
    }

    /**
     * Adds the filters.
     *
     * @param query the query
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param creatorUsername the creatorUsername
     * @param jobStatuses the jobStatuses
     */
    private void addFilters(StringBuilder query, Long jobId, String jobTypes, String creatorUsername, String jobStatuses) {
        query.append(" where 1=1 ");
        query.append((jobId != null) ? " and jobs.id = :jobId " : "");
        query.append(StringUtils.isNotBlank(jobTypes) ? " and jobs.job_type in :jobType " : "");
        query.append(StringUtils.isNotBlank(creatorUsername) ? " and jobs.creator_username = :creatorUsername " : "");
        query.append(StringUtils.isNotBlank(jobStatuses) ? " and jobs.job_status in :jobStatus " : "");
    }

    /**
     * Adds the parameters.
     *
     * @param query the query
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param creatorUsername the creatorUsername
     * @param jobStatuses the jobStatuses
     */
    private void addParameters(Query query, Long jobId, String jobTypes, String creatorUsername, String jobStatuses) {
        if(jobId != null){
            query.setParameter("jobId", jobId);
        }
        if(StringUtils.isNotBlank(jobTypes)){
            query.setParameter("jobType", Arrays.asList(jobTypes.split(",")));
        }
        if (StringUtils.isNotBlank(creatorUsername)) {
            query.setParameter("creatorUsername", creatorUsername);
        }
        if(StringUtils.isNotBlank(jobStatuses)){
            query.setParameter("jobStatus", Arrays.asList(jobStatuses.split(",")));
        }
    }
}
