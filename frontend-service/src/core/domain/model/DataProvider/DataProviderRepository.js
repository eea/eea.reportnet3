import { ApiDataProviderRepository } from 'core/infrastructure/domain/model/DataProvider/ApiDataProviderRepository';

export const DataProviderRepository = {
  all: () => Promise.reject('[DataProviderRepository#all] must be implemented'),
  allRepresentativesOf: () => Promise.reject('[DataProviderRepository#allRepresentativesOf] must be implemented'),
  add: () => Promise.reject('[DataProviderRepository#addByLogin] must be implemented'),
  deleteById: () => Promise.reject('[DataProviderRepository#deleteById] must be implemented'),
  update: () => Promise.reject('[DataProviderRepository#updateById] must be implemented')
};

export const dataProviderRepository = Object.assign({}, DataProviderRepository, ApiDataProviderRepository);
