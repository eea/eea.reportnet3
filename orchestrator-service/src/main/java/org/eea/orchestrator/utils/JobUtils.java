package org.eea.orchestrator.utils;


import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
       else if(name.equals("providerId")){
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
        } catch (EEAException e) {
            LOG.error("Error while releasing {} notification for jobId {} and datasetId {} ", eventType.getKey(), job.getId(), job.getDatasetId(), e);
        }
    }
}
