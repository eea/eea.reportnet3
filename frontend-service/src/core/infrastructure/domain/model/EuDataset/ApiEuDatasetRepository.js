import { apiEuDataset } from 'core/infrastructure/api/domain/model/EuDataset';

const copyDataCollection = async dataflowId => await apiEuDataset.copyDataCollection(dataflowId);

const exportEuDataset = async dataflowId => await apiEuDataset.exportEuDataset(dataflowId);

export const ApiEuDatasetRepository = { copyDataCollection, exportEuDataset };
