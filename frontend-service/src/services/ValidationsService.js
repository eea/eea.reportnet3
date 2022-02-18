import sortBy from 'lodash/sortBy';

import { ValidationsRepository } from 'repositories/ValidationsRepository';

export const ValidationsService = {
  getValidationsStatuses: async ({ filterBy, numberRows, pageNum, sortBy }) => {
    const validationsDTO = await ValidationsRepository.getValidationsStatuses({
      filterBy,
      isAsc: true, // TODO parser?
      numberRows,
      pageNum,
      sortBy
    });

    return validationsDTO;
  }
};
