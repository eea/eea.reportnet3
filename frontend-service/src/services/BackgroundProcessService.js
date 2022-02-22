import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

import { BackgroundProcessUtils } from 'services/_utils/BackgroundProcessUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ dataflowId, numberRows, pageNum, sortField, sortOrder, status, user }) => {
    const parsedSortField = BackgroundProcessUtils.parseSortField(sortField);

    return await BackgroundProcessRepository.getValidationsStatuses({
      dataflowId,
      numberRows,
      pageNum,
      sortField: parsedSortField,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      status,
      user
    });
  }
};
