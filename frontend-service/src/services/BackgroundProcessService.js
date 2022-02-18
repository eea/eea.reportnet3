import sortBy from 'lodash/sortBy';

import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ filterBy, numberRows, pageNum, sortBy }) => {
    const validationsDTO = await BackgroundProcessRepository.getValidationsStatuses({
      filterBy,
      isAsc: true, // TODO parser?
      numberRows,
      pageNum,
      sortBy
    });

    return validationsDTO;
  }
};
