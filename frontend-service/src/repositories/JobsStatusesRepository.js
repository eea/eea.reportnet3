import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { JobsStatusesConfig } from './config/JobStatusesConfig';

export const JobsStatusesRepository = {
  getJobsStatuses: async ({
    pageNum,
    numberRows,
    sortOrder,
    sortField = '',
    jobId,
    jobType,
    dataflowId,
    dataflowName,
    providerId,
    datasetId,
    datasetName,
    creatorUsername,
    jobStatus,
    code
  }) =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobsStatuses, {
        pageNum,
        numberRows,
        sortOrder,
        sortField,
        jobId,
        jobType,
        dataflowId,
        dataflowName,
        providerId,
        datasetId,
        datasetName,
        creatorUsername,
        jobStatus,
        code
      })
    }),

  getJobHistory: async jobId =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobHistory, {
        jobId
      })
    }),

  getJobsHistory: async ({
    pageNum,
    numberRows,
    sortOrder,
    sortField = '',
    jobId,
    jobType,
    dataflowId,
    dataflowName,
    providerId,
    datasetId,
    datasetName,
    creatorUsername,
    jobStatus
  }) =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobsHistory, {
        pageNum,
        numberRows,
        sortOrder,
        sortField,
        jobId,
        jobType,
        dataflowId,
        dataflowName,
        providerId,
        datasetId,
        datasetName,
        creatorUsername,
        jobStatus
      })
    }),
  getCancelledValidations: async ({ jobId, pageNum, numberRows, sortOrder, sortField = '' }) =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getCancelledValidations, {
        jobId,
        pageNum,
        numberRows,
        sortOrder,
        sortField
      })
    }),

  cancelJob: async (jobId, dataflowId, datasetId) => {

    if (datasetId == null) {
      const url = getUrl(JobsStatusesConfig.cancelJobNoDataset, { jobId, dataflowId });
      return await HTTPRequester.update({ url });
    }
    const url = getUrl(JobsStatusesConfig.cancelJob, { jobId, dataflowId, datasetId });
    return await HTTPRequester.update({ url });
  }
};
