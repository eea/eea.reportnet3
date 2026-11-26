package org.eea.orchestrator.utils;


import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class JobUtils {

    @Autowired
    private DataFlowControllerZuul dataFlowControllerZuul;
    @Autowired
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;
    @Autowired
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobUtils.class);

    public String getJobColumnNameByObjectName(String name) {
       String columnName = null;
       if(name == null){
           return null;
       }

       if(name.equals("jobId")){
           columnName = "id";
       }
       else if(name.equals("jobType")){
           columnName = "job_type";
       }
       else if(name.equals("jobStatus")){
           columnName = "job_status";
       }
       else if(name.equals("dateAdded")){
           columnName = "date_added";
       }
       else if(name.equals("dateStatusChanged")){
           columnName = "date_status_changed";
       }
       else if(name.equals("parameters")){
           columnName = "parameters";
       }
       else if(name.equals("creatorUsername")){
           columnName = "creator_username";
       }
       else if(name.equals("dataflowId")){
           columnName = "dataflow_id";
       }
       else if(name.equals("providerId") || name.equals("providerName")){
           columnName = "provider_id";
       }
       else if(name.equals("datasetId")){
           columnName = "dataset_id";
       }
       else if(name.equals("fmeJobId")){
           columnName = "fme_job_id";
       }
       else if(name.equals("dataflowName")){
           columnName = "dataflow_name";
       }
       else if(name.equals("datasetName")){
           columnName = "dataset_name";
       }
       else{
           LOG.info("Could not match jobs header {} to a column in the table", name);
       }
       return columnName;
    }

    public void sendKafkaImportNotification(JobVO job, EventType eventType, String error){
        Map<String, Object> value = new HashMap<>();
        String user = job.getCreatorUsername();
        value.put(LiteralConstants.USER, user);
        Map<String, Object> insertedParameters = job.getParameters();
        String fileName = (String) insertedParameters.get("fileName");
        String tableSchemaName = null;
        String tableSchemaId = null;
        String dataflowName = job.getDataflowName();
        String datasetName = job.getDatasetName();

        if(dataflowName == null) {
            try {
                dataflowName = dataFlowControllerZuul.findDataflowNameById(job.getDataflowId());
            } catch (Exception e) {
                LOG.error("Error when trying to receive dataflow name for dataflowId {} ", job.getDataflowId(), e);
            }
        }

        if(datasetName == null) {
            try {
                datasetName = dataSetMetabaseControllerZuul.findDatasetNameById(job.getDatasetId());
            } catch (Exception e) {
                LOG.error("Error when trying to receive dataset name for datasetId {} ", job.getDatasetId(), e);
            }
        }

        String datasetSchemaId = null;
        try {
            datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(job.getDatasetId());
        } catch (Exception e) {
            LOG.error("Error when trying to receive dataset schema id for datasetId {} ", job.getDatasetId(), e);
        }

        if(insertedParameters.get("tableSchemaId") != null) {
            tableSchemaId = (String) insertedParameters.get("tableSchemaId");
            if (tableSchemaId != null && datasetSchemaId != null) {
                try {
                    tableSchemaName = datasetSchemaControllerZuul.getTableSchemaName(datasetSchemaId, tableSchemaId);
                } catch (Exception e) {
                    LOG.error("Error when trying to receive table schema name for tableSchemaId {} datasetId {} and datasetSchemaId {} ", tableSchemaId, job.getDatasetId(), datasetSchemaId, e);
                }
            }
        }

        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value,
                    NotificationVO.builder().datasetId(job.getDatasetId()).dataflowId(job.getDataflowId()).tableSchemaId(tableSchemaId).fileName(fileName)
                            .dataflowName(dataflowName).datasetName(datasetName).tableSchemaName(tableSchemaName)
                            .user(user).error(error).build());
        } catch (Exception e) {
            LOG.error("Error while releasing {} notification for jobId {} and datasetId {} ", eventType.getKey(), job.getId(), job.getDatasetId(), e);
        }
    }

    /**
     * Global roles allowed to see every job creator's username.
     * Mirrors the frontend's (isAdmin || isCustodian) check in JobsStatuses.jsx.
     */
    private static final Set<String> USERNAME_VISIBLE_ROLES = Set.of(
            "ROLE_" + SecurityRoleEnum.ADMIN,
            "ROLE_" + SecurityRoleEnum.DATA_CUSTODIAN,
            "ROLE_" + SecurityRoleEnum.DATA_STEWARD);

    /**
     * Checks whether the current user may see the creator username of every job.
     *
     * @return true if the user has the global ADMIN, DATA_CUSTODIAN or DATA_STEWARD role
     */
    public boolean canSeeAllCreatorUsernames() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(USERNAME_VISIBLE_ROLES::contains);
    }

    /**
     * Removes the creator username from the items that were not created by the current user,
     * unless the current user is allowed to see all creator usernames.
     * The current user's own username is kept, as the frontend needs it to enable the cancel button.
     *
     * @param items the items to process (e.g. JobVO or JobHistoryVO)
     * @param getter the creator username getter
     * @param setter the creator username setter
     * @param <T> the item type
     */
    public <T> void hideOtherCreatorUsernames(List<T> items, Function<T, String> getter,
                                              BiConsumer<T, String> setter) {
        if (items != null && !items.isEmpty() && !canSeeAllCreatorUsernames()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication != null ? authentication.getName() : null;
            for (T item : items) {
                if (currentUsername == null || !currentUsername.equals(getter.apply(item))) {
                    setter.accept(item, null);
                }
            }
        }
    }
}
