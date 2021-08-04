import { euDatasetRepository } from 'repositories/EuDatasetRepository';

const copyDataCollection = async dataflowId => await euDatasetRepository.copyDataCollection(dataflowId);

const exportEuDataset = async dataflowId => await euDatasetRepository.exportEuDataset(dataflowId);

export const EuDatasetService = { copyDataCollection, exportEuDataset };
