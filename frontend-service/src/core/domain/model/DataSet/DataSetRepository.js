import { ApiDatasetRepository } from 'core/infrastructure/domain/model/DataSet/ApiDataSetRepository';

export const DatasetRepository = {
  schemaById: () => Promise.reject('[DatasetRepository#schemaById] must be implemented'),
  deleteDataById: () => Promise.reject('[DatasetRepository#deleteDataById] must be implemented'),
  deleteTableDataById: () => Promise.reject('[DatasetRepository#deleteTableDataById] must be implemented'),
  errorsById: () => Promise.reject('[DatasetRepository#errorsById] must be implemented'),
  errorPositionByObjectId: () => Promise.reject('[DatasetRepository#errorPositionByObjectId] must be implemented'),
  errorStatisticsById: () => Promise.reject('[DatasetRepository#errorStatisticsById] must be implemented'),
  exportDataById: () => Promise.reject('[DatasetRepository#exportDataById] must be implemented'),
  exportTableDataById: () => Promise.reject('[DatasetRepository#exportTableDataById] must be implemented'),
  tableDataById: () => Promise.reject('[DatasetRepository#tableDataById] must be implemented'),
  validateDataById: () => Promise.reject('[DatasetRepository#validateDataById] must be implemented'),
  webFormDataById: () => Promise.reject('[DatasetRepository#webFormDataById] must be implemented')
};

export const datasetRepository = Object.assign({}, DatasetRepository, ApiDatasetRepository);
