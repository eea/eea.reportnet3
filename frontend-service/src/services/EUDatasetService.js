import { EUDatasetRepository } from 'repositories/EUDatasetRepository';

export const EUDatasetService = {
  copyFromDataCollection: async dataflowId => await EUDatasetRepository.copyFromDataCollection(dataflowId),

  export: async dataflowId => await EUDatasetRepository.export(dataflowId)
};
