import { JobsStatusesRepository } from 'repositories/JobsStatusesRepository';

import { JobsStatusesUtils } from './_utils/JobsStatusesUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const JobsStatusesService = {
  getJobsStatuses: async ({
    pageNum,
    numberRows,
    sortOrder,
    sortField,
    jobId,
    jobType,
    dataflowId,
    dataflowName,
    providerId,
    providerName,
    datasetId,
    datasetName,
    creatorUsername,
    jobStatus,
    code
  }) => {
    const parsedSortField = JobsStatusesUtils.parseSortField(sortField);

    const response = await JobsStatusesRepository.getJobsStatuses({
      pageNum,
      numberRows,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      sortField: parsedSortField,
      jobId,
      jobType,
      dataflowId,
      dataflowName,
      providerId,
      providerName,
      datasetId,
      datasetName,
      creatorUsername,
      jobStatus,
      code
    });

    return response.data;
  },

  getJobHistory: async jobId => {
    const response = await JobsStatusesRepository.getJobHistory(jobId);

    return response.data;
  },

  getJobsHistory: async ({
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
  }) => {
    const parsedSortField = JobsStatusesUtils.parseSortField(sortField);

    const response = await JobsStatusesRepository.getJobsHistory({
      pageNum,
      numberRows,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      sortField: parsedSortField,
      jobId,
      jobType,
      dataflowId,
      dataflowName,
      providerId,
      datasetId,
      datasetName,
      creatorUsername,
      jobStatus
    });

    return response.data;
  },

  getCancelledValidations: async ({ jobId, pageNum, numberRows, sortOrder, sortField }) => {
    const parsedSortField = JobsStatusesUtils.parseSortField(sortField);
    const response = await JobsStatusesRepository.getCancelledValidations({
      jobId,
      pageNum,
      numberRows,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      sortField: parsedSortField
    });
    return response.data;
  },

  cancelJob: async (jobId, dataflowId, datasetId) => {
    await JobsStatusesRepository.cancelJob(jobId, dataflowId, datasetId);
  }
};
