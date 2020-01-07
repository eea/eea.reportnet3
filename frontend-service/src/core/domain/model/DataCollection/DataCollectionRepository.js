import { ApiDataCollectionRepository } from 'core/infrastructure/domain/model/DataCollection/ApiDataCollectionRepository';

export const DataCollectionRepository = {
  create: () => Promise.reject('[DataCollectionRepository#create] must be implemented')
};

export const dataCollectionRepository = Object.assign({}, DataCollectionRepository, ApiDataCollectionRepository);
