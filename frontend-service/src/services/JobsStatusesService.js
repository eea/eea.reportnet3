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
    creatorUsername,
    jobStatus}) => {
      const parsedSortField = JobsStatusesUtils.parseSortField(sortField);

      const response = await JobsStatusesRepository.getJobsStatuses({
        pageNum,
        numberRows,
        sortOrder: ServiceUtils.getSortOrder(sortOrder),
        sortField: parsedSortField,
        jobId,
        jobType,
        creatorUsername,
        jobStatus
      });

      return response.data;
    }

};
