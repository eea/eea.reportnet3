import { isNil } from 'lodash/isNil';

import { BackgroundProcessConfig } from './config/BackgroundProcessConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

const getSortOrder = sortOrder => {
  console.log('sortOrder', sortOrder);
  if (sortOrder === -1) {
    return 0;
  } else if (isNil(sortOrder)) {
    return undefined;
  } else {
    return sortOrder;
  }
};

export const BackgroundProcessRepository = {
  getValidationsStatuses: async ({ sortOrder, numberRows, pageNum, sortField = '', user, dataflowId, status }) => {
    return await HTTPRequester.get({
      url: getUrl(BackgroundProcessConfig.getValidationsStatuses, {
        // sortOrder: getSortOrder(sortOrder),
        sortOrder: sortOrder,
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
