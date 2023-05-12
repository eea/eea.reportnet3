import { HistoricReleaseRepository } from 'repositories/HistoricReleaseRepository';

import { HistoricReleaseUtils } from 'services/_utils/HistoricReleaseUtils';

export const HistoricReleaseService = {
  getAll: async datasetId => {
    console.log('dataset id is: ' + datasetId);
    const response = await HistoricReleaseRepository.getAll(datasetId);
    return HistoricReleaseUtils.parseHistoricReleaseListDTO(response.data);
  },

  getAllRepresentative: async (dataflowId, dataProviderId) => {
    console.log('dataflow id is: ' + dataflowId);
    const response = await HistoricReleaseRepository.getAllRepresentative(dataflowId, dataProviderId);
    return HistoricReleaseUtils.parseHistoricReleaseListDTO(response.data);
  }
};
