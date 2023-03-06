export const JobsStatusesConfig = {
  getJobsStatuses:
    '/orchestrator/jobs/?pageNum={:pageNum}&pageSize={:numberRows}&asc={:sortOrder}&sortedColumn={:sortField}&jobId={:jobId}&jobType={:jobType}&dataflowId={:dataflowId}&dataflowName={:dataflowName}&providerId={:providerId}&datasetId={:datasetId}&datasetName={:datasetName}&creatorUsername={:creatorUsername}&jobStatus={:jobStatus}',
  getJobHistory: '/orchestrator/jobHistory/{:jobId}',
  cancelJob: '/orchestrator/jobs/cancelJob/{:jobId}'
};
