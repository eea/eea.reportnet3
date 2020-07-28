import { ApiEuDatasetRepository } from 'core/infrastructure/domain/model/EuDataset/ApiEuDatasetRepository';

export const EuDatasetRepository = {
  copyDataCollection: () => Promise.reject('[EuDatasetRepository#copyDataCollection] must be implemented'),
  exportEuDataset: () => Promise.reject('[EuDatasetRepository#exportEuDataset] must be implemented')
};

export const euDatasetRepository = Object.assign({}, EuDatasetRepository, ApiEuDatasetRepository);
