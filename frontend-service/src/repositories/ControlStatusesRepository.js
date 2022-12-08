import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { ControlStatusesConfig } from './config/ControlStatusesConfig';

export const ControlStatusesRepository = {
  getControlStatuses: async ({
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
      url: getUrl(ControlStatusesConfig.getControlStatuses, {
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
