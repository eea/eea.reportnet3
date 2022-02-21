import { BackgroundProcessConfig } from './config/BackgroundProcessConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const BackgroundProcessRepository = {
  getValidationsStatuses: async ({ sortOrder, numberRows, pageNum, sortField = '', user, dataflowId, status }) => {
    return await HTTPRequester.get({
      url: getUrl(BackgroundProcessConfig.getValidationsStatuses, {
        sortOrder,
        numberRows,
        pageNum,
        sortField,
        user,
        dataflowId,
        status
      })
    });
  }
};
