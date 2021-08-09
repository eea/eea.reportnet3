import { HistoricReleaseRepository } from 'repositories/HistoricReleaseRepository';

import { HistoricReleaseUtils } from 'services/_utils/HistoricReleaseUtils';

export const HistoricReleaseService = {
  getAll: async datasetId => {
    const response = await HistoricReleaseRepository.getAll(datasetId);
    return HistoricReleaseUtils.parseHistoricReleaseListDTO(response.data);
  },

  getAllRepresentative: async (dataflowId, dataProviderId) => {
    const response = await HistoricReleaseRepository.getAllRepresentative(dataflowId, dataProviderId);
    return HistoricReleaseUtils.parseHistoricReleaseListDTO(response.data);
  }
};
