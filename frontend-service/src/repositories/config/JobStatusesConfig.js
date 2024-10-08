export const JobsStatusesConfig = {
  getJobsStatuses:
    '/orchestrator/jobs/?pageNum={:pageNum}&pageSize={:numberRows}&asc={:sortOrder}&sortedColumn={:sortField}&jobId={:jobId}&jobType={:jobType}&dataflowId={:dataflowId}&dataflowName={:dataflowName}&providerId={:providerId}&datasetId={:datasetId}&datasetName={:datasetName}&creatorUsername={:creatorUsername}&jobStatus={:jobStatus}',
  getJobHistory: '/orchestrator/jobHistory/{:jobId}',
  getJobsHistory:
    '/orchestrator/jobHistory/?pageNum={:pageNum}&pageSize={:numberRows}&asc={:sortOrder}&sortedColumn={:sortField}&jobId={:jobId}&jobType={:jobType}&dataflowId={:dataflowId}&dataflowName={:dataflowName}&providerId={:providerId}&datasetId={:datasetId}&datasetName={:datasetName}&creatorUsername={:creatorUsername}&jobStatus={:jobStatus}',
  cancelJob: '/orchestrator/jobs/cancelJob/{:jobId}'
};
