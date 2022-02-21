import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

import { BackgroundProcessUtils } from 'services/_utils/BackgroundProcessUtils';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ dataflowId, numberRows, pageNum, sortField, sortOrder, status, user }) => {
    const parsedSortField = BackgroundProcessUtils.parseSortField(sortField);
    const validationsDTO = await BackgroundProcessRepository.getValidationsStatuses({
      dataflowId,
      numberRows,
      pageNum,
      sortField,
      sortOrder,
      sortField: parsedSortField,
      status,
      user
    });

    return validationsDTO;
  }
};
