import { apiDataCollection } from 'core/infrastructure/api/domain/model/DataCollection';

const create = async (dataflowId, endDate, stopAndNotifySQLErrors) => {
  return await apiDataCollection.create(dataflowId, endDate, stopAndNotifySQLErrors);
};

const update = async dataflowId => {
  return await apiDataCollection.update(dataflowId);
};

export const ApiDataCollectionRepository = {
  create,
  update
};
