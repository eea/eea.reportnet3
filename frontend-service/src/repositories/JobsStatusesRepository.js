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
    jobStatus}) =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobsStatuses,{
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
    })

};
