import { EuDatasetRepository } from 'repositories/EuDatasetRepository';

export const EuDatasetService = {
  copyFromDataCollection: async dataflowId => await EuDatasetRepository.copyFromDataCollection(dataflowId),

  export: async dataflowId => await EuDatasetRepository.export(dataflowId)
};
