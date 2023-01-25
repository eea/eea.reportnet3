import { ControlStatusesRepository } from 'repositories/ControlStatusesRepository';

export const ControlStatusesService = {
  getDatasetData: async (datasetId, dataProviderId) => {
    const response = await ControlStatusesRepository.getDatasetData(datasetId, dataProviderId);

    return response.data;
  },

  deleteDatasetData: async datasetId => {
    const response = await ControlStatusesRepository.deleteDatasetData(datasetId);

    return response.data;
  }
};
