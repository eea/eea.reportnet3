import { apiDataCollection } from 'core/infrastructure/api/domain/model/DataCollection';

const create = async (dataflowId, schemaId, dataCollectionName, creationDate) => {
  return await apiDataCollection.create(dataflowId, schemaId, dataCollectionName, creationDate);
};

export const ApiDataCollectionRepository = {
  create
};
