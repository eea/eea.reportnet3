import { apiDataCollection } from 'core/infrastructure/api/domain/model/DataCollection';

const create = async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) => {
  return await apiDataCollection.create(
    dataflowId,
    endDate,
    isManualTechnicalAcceptance,
    stopAndNotifySQLErrors,
    showPublicInfo
  );
};

const update = async dataflowId => {
  return await apiDataCollection.update(dataflowId);
};

export const ApiDataCollectionRepository = {
  create,
  update
};
