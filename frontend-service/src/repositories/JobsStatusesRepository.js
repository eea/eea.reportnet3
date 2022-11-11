import { BackgroundProcessConfig } from './config/BackgroundProcessConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { JobsStatusesConfig } from './config/JobStatusesConfig';

export const JobsStatusesRepository = {
  getJobsStatuses: async () =>
    await HTTPRequester.get({
      url: getUrl(JobsStatusesConfig.getJobsStatuses,{})
    })

};
