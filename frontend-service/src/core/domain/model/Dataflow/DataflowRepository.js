import { ApiDataflowRepository } from 'core/infrastructure/domain/model/Dataflow/ApiDataflowRepository';

export const DataflowRepository = {
  accept: () => Promise.reject('[DataflowRepository#accept] must be implemented'),
  accepted: () => Promise.reject('[DataflowRepository#accepted] must be implemented'),
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  cloneDatasetSchemas: () => Promise.reject('[DataflowRepository#cloneDatasetSchemas] must be implemented'),
  completed: () => Promise.reject('[DataflowRepository#completed] must be implemented'),
  create: () => Promise.reject('[DataflowRepository#create] must be implemented'),
  datasetsFinalFeedback: () => Promise.reject('[DataflowRepository#datasetsFinalFeedback] must be implemented'),
  datasetsReleasedStatus: () => Promise.reject('[DataflowRepository#datasetsReleasedStatus] must be implemented'),
  datasetsValidationStatistics: () =>
    Promise.reject('[DataflowRepository#datasetsValidationStatistics] must be implemented'),
  deleteById: () => Promise.reject('[DataflowRepository#deleteById] must be implemented'),
  generateApiKey: () => Promise.reject('[DataflowRepository#generateApiKey] must be implemented'),
  getAllSchemas: () => Promise.reject('[DataflowRepository#getAllSchemas] must be implemented'),
  getApiKey: () => Promise.reject('[DataflowRepository#getApiKey] must be implemented'),
  getPublicDataflowInformation: () => Promise.reject('[DataflowRepository#getPublicDataflowInformation] must be implemented'),
  metadata: () => Promise.reject('[DataflowRepository#metadata] must be implemented'),
  newEmptyDatasetSchema: () => Promise.reject('[DataflowRepository#newEmptyDatasetSchema] must be implemented'),
  pending: () => Promise.reject('[DataflowRepository#pending] must be implemented'),
  reject: () => Promise.reject('[DataflowRepository#reject] must be implemented'),
  reporting: () => Promise.reject('[DataflowRepository#reporting] must be implemented'),
  schemasValidation: () => Promise.reject('[DataflowRepository#schemasValidation] must be implemented'),
  update: () => Promise.reject('[DataflowRepository#update] must be implemented')
};

export const dataflowRepository = Object.assign({}, DataflowRepository, ApiDataflowRepository);
