import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

import { BackgroundProcessUtils } from 'services/_utils/BackgroundProcessUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ dataflowId, numberRows, pageNum, sortField, sortOrder, status, user }) => {
    const parsedSortField = BackgroundProcessUtils.parseSortField(sortField);

    const response = await BackgroundProcessRepository.getValidationsStatuses({
      dataflowId,
      numberRows,
      pageNum,
      sortField: parsedSortField,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      status,
      user
    });

    response.data.processList = BackgroundProcessUtils.parseValidationsStatusListDTO(response.data.processList);

    return response.data;
  },

  update: async ({ processId, priority }) => await BackgroundProcessRepository.update({ processId, priority })
};
