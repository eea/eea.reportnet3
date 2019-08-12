import { ApiDataSetRepository } from 'core/infrastructure/domain/model/DataSet/ApiDataSetRepository';

export const DataSetRepository = {
  schemaById: () => Promise.reject('[DataSetRepository#schemaById] must be implemented'),
  deleteDataById: () => Promise.reject('[DataSetRepository#deleteDataById] must be implemented'),
  errorsById: () => Promise.reject('[DataSetRepository#errorsById] must be implemented'),
  errorPositionByObjectId: () => Promise.reject('[DataSetRepository#errorPositionByObjectId] must be implemented'),
  errorStatisticsById: () => Promise.reject('[DataSetRepository#errorStatisticsById] must be implemented'),
  validateDataById: () => Promise.reject('[DataSetRepository#validateDataById] must be implemented')
};

export const dataSetRepository = Object.assign({}, DataSetRepository, ApiDataSetRepository);
