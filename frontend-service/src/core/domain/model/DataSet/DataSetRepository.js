import { ApiDataSetRepository } from 'core/infrastructure/domain/model/DataSet/ApiDataSetRepository';

export const DataSetRepository = {
  errorsById: () => Promise.reject('[DataSetRepository#errorsById] must be implemented'),
  errorPositionByObjectId: () => Promise.reject('[DataSetRepository#errorPositionByObjectId] must be implemented')
};

export const dataSetRepository = Object.assign({}, DataSetRepository, ApiDataSetRepository);
