import { BackgroundProcessRepository } from 'repositories/BackgroundProcessRepository';

export const BackgroundProcessService = {
  getValidationsStatuses: async ({ dataflowId, numberRows, pageNum, sortField, sortOrder, status, user }) => {
    const validationsDTO = await BackgroundProcessRepository.getValidationsStatuses({
      dataflowId,
      numberRows,
      pageNum,
      sortField,
      sortOrder,
      status,
      user
    });

    return validationsDTO;
  }
};
