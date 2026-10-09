package org.eea.orchestrator.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.orchestrator.mapper.JobHistoryMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.eea.orchestrator.persistence.domain.JobStatsDTO;
import org.eea.orchestrator.persistence.repository.JobHistoryRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.utils.JobUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobHistoryServiceImpl implements JobHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    /** The job history mapper. */
    @Autowired
    private JobHistoryMapper jobHistoryMapper;

    /** The job Utils. */
    @Autowired
    private JobUtils jobUtils;

    @Autowired
    private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;

    @Transactional
    @Override
    public void saveJobHistory(Job job){
        JobHistory entry = new JobHistory(null, job.getId(), job.getJobType(), job.getJobStatus(), job.getDateAdded(), job.getDateStatusChanged(), job.getParameters(), job.getCreatorUsername(), job.isRelease(), job.getDataflowId(), job.getProviderId(), job.getDatasetId(),job.getFmeJobId(), job.getDataflowName(), job.getDatasetName(), job.getJobInfo(), job.getFmeStatus());
        jobHistoryRepository.save(entry);
    }

    @Override
    public List<JobHistoryVO> getJobHistory(Long jobId){
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAllByJobIdOrderById(jobId);
        return jobHistoryMapper.entityListToClass(jobHistoryList);
    }

    @Override
    public JobsHistoryVO getJobHistory(Pageable pageable, boolean asc, String sortedColumn,
                                       Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                                       String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses) {

        String sortedTableColumn = jobUtils.getJobColumnNameByObjectName(sortedColumn);
        String dataProviderIds = "";
        // Resolve the human-readable providerName filter (from the request) into the internal providerId
        // used by the query, since jobs store providerId, not provider label.
        if (StringUtils.isNotBlank(providerName)) {
            List<DataProviderVO> dataProviders = representativeControllerZuul.findDataProvidersByLabel(providerName);
            if (dataProviders != null && !dataProviders.isEmpty()) {
                dataProviderIds = dataProviders.stream()
                        .map(name -> String.valueOf(name.getId()))
                        .collect(Collectors.joining(","));
            } else {
                // No provider matches the given providerName, so the filter can never match any real job.
                // Force jobId to a sentinel value (0L, an id that can never exist) to guarantee the
                // downstream query returns an empty result set.
                // Without this, an unmatched providerName would be silently ignored and the query would
                // fall back to fetching ALL jobs - returning incorrect, unfiltered results to the caller.
                jobId = 0L;
            }
        }



        List<JobHistory> jobHistoryList = jobHistoryRepository.findJobHistoryPaginated(pageable, asc, sortedTableColumn, jobId, jobTypes, dataflowId, dataflowName, dataProviderIds, datasetId, datasetName, creatorUsername, jobStatuses);
        List<JobHistoryVO> jobHistoryVOList = jobHistoryMapper.entityListToClass(jobHistoryList);

        populateProviderNames(jobHistoryVOList);

        JobsHistoryVO jobsHistoryVO = new JobsHistoryVO();
        jobsHistoryVO.setTotalRecords(jobHistoryRepository.count());
        jobsHistoryVO.setFilteredRecords(jobHistoryRepository.countJobHistoryPaginated(asc, sortedTableColumn, jobId, jobTypes, dataflowId, dataflowName, dataProviderIds, datasetId, datasetName, creatorUsername, jobStatuses));
        jobsHistoryVO.setFilteredJobs(jobHistoryRepository.countFilteredJobs( jobId, jobTypes, dataflowId, dataflowName, dataProviderIds, datasetId, datasetName, creatorUsername, jobStatuses));
        jobsHistoryVO.setJobHistoryVOList(jobHistoryVOList);

        return jobsHistoryVO;
    }

    @Override
    public void updateJobInfoOfLastHistoryEntry(Long jobId, JobInfoEnum jobInfo, Integer lineNumber){
        Optional<JobHistory> optionalJobHistory = jobHistoryRepository.findFirstByJobIdOrderByIdDesc(jobId);
        if(optionalJobHistory.isPresent()){
            String jobInfoStr = null;
            if(jobInfo != null) {
                jobInfoStr = jobInfo.getValue(lineNumber);
            }
            optionalJobHistory.get().setJobInfo(jobInfoStr);
            jobHistoryRepository.save(optionalJobHistory.get());
        }
    }

    @Override
    public String getJobStatsForYesterday() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        JSONArray daysArray = new JSONArray();

        LocalDate day = LocalDate.now().minusDays(1);
        String formattedDay = day.format(formatter);

        JSONObject dayObject = new JSONObject();
        dayObject.put("day", formattedDay);
        dayObject.put("data", new JSONObject(jobHistoryRepository.getJobStatsForPreviousDay(1).toString())); // Deep copy

        daysArray.put(dayObject);

        JSONObject dayObjectMinus2 = new JSONObject();
        day = LocalDate.now().minusDays(2);
        formattedDay = day.format(formatter);

        dayObjectMinus2.put("day", formattedDay);
        dayObjectMinus2.put("data", new JSONObject(jobHistoryRepository.getJobStatsForPreviousDay(2).toString())); // Deep copy

        daysArray.put(dayObjectMinus2);

        JSONObject finalObject = new JSONObject();
        finalObject.put("days", daysArray);

        return finalObject.toString(2);
    }

    /**
     * Enriches each JobVO with its provider's display name.
     * providerId has no DB foreign key to the provider table, so the name must be resolved via a separate call rather than a SQL join.
     * Resolved in a single batch call (rather than per-row) to avoid an N+1 remote-call problem against the representative service.
     */
    private void populateProviderNames(List<JobHistoryVO> jobHistoryVOList) {
        List<Long> providerIds = jobHistoryVOList.stream()
                .map(JobHistoryVO::getProviderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (providerIds.isEmpty()) {
            return;
        }

        Map<Long, String> labelById = representativeControllerZuul.findDataProvidersByIds(providerIds).stream()
                .collect(Collectors.toMap(DataProviderVO::getId, DataProviderVO::getLabel));

        jobHistoryVOList.forEach(jobHistory -> jobHistory.setProviderName(labelById.get(jobHistory.getProviderId())));
    }


}
