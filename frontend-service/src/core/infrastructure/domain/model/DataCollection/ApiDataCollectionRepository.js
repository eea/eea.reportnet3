import { apiDataCollection } from 'core/infrastructure/api/domain/model/DataCollection';

const create = async (dataflowId, endDate, isManualTechnicalAcceptance) => {
  return await apiDataCollection.create(dataflowId, endDate, isManualTechnicalAcceptance);
};

const update = async dataflowId => {
  return await apiDataCollection.update(dataflowId);
};

export const ApiDataCollectionRepository = {
  create,
  update
};
