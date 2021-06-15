import { ApiDataCollectionRepository } from 'core/infrastructure/domain/model/DataCollection/ApiDataCollectionRepository';

export const DataCollectionRepository = {
  create: () => Promise.reject('[DataCollectionRepository#create] must be implemented'),
  createReference: () => Promise.reject('[DataCollectionRepository#createReference] must be implemented'),
  update: () => Promise.reject('[DataCollectionRepository#update] must be implemented')
};

export const dataCollectionRepository = Object.assign({}, DataCollectionRepository, ApiDataCollectionRepository);
