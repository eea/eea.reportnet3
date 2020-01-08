import { apiDataCollection } from 'core/infrastructure/api/domain/model/DataCollection';

const create = async (dataflowId, endDate) => {
  return await apiDataCollection.create(dataflowId, endDate);
};

export const ApiDataCollectionRepository = {
  create
};
