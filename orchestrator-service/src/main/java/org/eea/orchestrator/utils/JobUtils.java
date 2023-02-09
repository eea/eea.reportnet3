package org.eea.orchestrator.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobUtils {

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
}
