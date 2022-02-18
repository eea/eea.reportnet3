import { ValidationsConfig } from './config/ValidationsConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ValidationsRepository = {
  getValidationsStatuses: async ({ filterBy, isAsc, numberRows, pageNum }) =>
    await HTTPRequester.get({
      url: getUrl(ValidationsConfig.getValidationsStatuses, { isAsc, numberRows, pageNum })
      // data: { ...filterBy }
    })
};
