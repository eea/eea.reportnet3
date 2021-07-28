import { ApiDataflowRepository } from 'core/infrastructure/domain/model/Dataflow/ApiDataflowRepository';

export const DataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  cloneDatasetSchemas: () => Promise.reject('[DataflowRepository#cloneDatasetSchemas] must be implemented'),
  create: () => Promise.reject('[DataflowRepository#create] must be implemented'),
  dataflowDetails: () => Promise.reject('[DataflowRepository#dataflowDetails] must be implemented'),
  datasetsFinalFeedback: () => Promise.reject('[DataflowRepository#datasetsFinalFeedback] must be implemented'),
  datasetsReleasedStatus: () => Promise.reject('[DataflowRepository#datasetsReleasedStatus] must be implemented'),
  datasetsValidationStatistics: () =>
    Promise.reject('[DataflowRepository#datasetsValidationStatistics] must be implemented'),
  deleteById: () => Promise.reject('[DataflowRepository#deleteById] must be implemented'),
  downloadById: () => Promise.reject('[DataflowRepository#downloadById] must be implemented'),
  generateApiKey: () => Promise.reject('[DataflowRepository#generateApiKey] must be implemented'),
  getAllSchemas: () => Promise.reject('[DataflowRepository#getAllSchemas] must be implemented'),
  getApiKey: () => Promise.reject('[DataflowRepository#getApiKey] must be implemented'),
  getAllDataflowsUserList: () => Promise.reject('[DataflowRepository#getAllDataflowsUserList] must be implement'),
  getRepresentativesUsersList: () =>
    Promise.reject('[DataflowRepository#getRepresentativesUsersList] must be implement'),
  getUserList: () => Promise.reject('[DataflowRepository#getUserlist] must be implement'),
  getPublicDataflowData: () => Promise.reject('[DataflowRepository#getPublicDataflowData] must be implemented'),
  getPublicDataflowsByCountryCode: () =>
    Promise.reject('[DataflowRepository#getPublicDataflowsByCountryCode] must be implemented'),
  metadata: () => Promise.reject('[DataflowRepository#metadata] must be implemented'),
  newEmptyDatasetSchema: () => Promise.reject('[DataflowRepository#newEmptyDatasetSchema] must be implemented'),
  reporting: () => Promise.reject('[DataflowRepository#reporting] must be implemented'),
  schemasValidation: () => Promise.reject('[DataflowRepository#schemasValidation] must be implemented'),
  update: () => Promise.reject('[DataflowRepository#update] must be implemented')
};

export const dataflowRepository = Object.assign({}, DataflowRepository, ApiDataflowRepository);
