import isNil from 'lodash/isNil';

import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

import { BackgroundProcessUtils } from 'services/_utils/BackgroundProcessUtils';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ dataflowId, numberRows, pageNum, sortField, sortOrder, status, user }) => {
    const parsedSortField = BackgroundProcessUtils.parseSortField(sortField);

    const getSortOrder = sortOrder => {
      if (sortOrder === -1) {
        return 0;
      } else if (isNil(sortOrder)) {
        return undefined;
      } else {
        return sortOrder;
      }
    };

    const validationsDTO = await BackgroundProcessRepository.getValidationsStatuses({
      dataflowId,
      numberRows,
      pageNum,
      sortField: parsedSortField,
      sortOrder: getSortOrder(sortOrder),
      status,
      user
    });

    return validationsDTO;
  }
};
