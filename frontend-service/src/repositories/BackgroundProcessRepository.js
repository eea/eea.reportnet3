import { BackgroundProcessConfig } from './config/BackgroundProcessConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const BackgroundProcessRepository = {
  getValidationsStatuses: async ({ filterBy, isAsc, numberRows, pageNum }) =>
    await HTTPRequester.get({
      url: getUrl(BackgroundProcessConfig.getValidationsStatuses, { isAsc, numberRows, pageNum }),
      data: { ...filterBy }
    })
};
