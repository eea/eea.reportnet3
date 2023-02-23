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
    providerId,
    datasetId,
    creatorUsername,
    jobStatus
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
        providerId,
        datasetId,
        creatorUsername,
        jobStatus
      })
    }),

  getJobHistory: async jobId =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobHistory, {
        jobId
      })
    }),

  cancelJob: async jobId =>
    await HTTPRequester.update({
      url: getUrl(JobsStatusesConfig.cancelJob, {
        jobId
      })
    })
};
