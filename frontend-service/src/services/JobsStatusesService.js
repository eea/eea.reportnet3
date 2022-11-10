import { JobsStatusesRepository } from 'repositories/JobsStatusesRepository';

import { BackgroundProcessUtils } from 'services/_utils/BackgroundProcessUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const JobsStatusesService = {
  getJobsStatuses: async () => {
    const response = await JobsStatusesRepository.getJobsStatuses();
    return response.data;
  }

};
