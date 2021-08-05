import { dataCollectionRepository } from 'repositories/DataCollectionRepository';

const create = async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) => {
  return await dataCollectionRepository.create(
    dataflowId,
    endDate,
    isManualTechnicalAcceptance,
    stopAndNotifySQLErrors,
    showPublicInfo
  );
};

const createReference = async (dataflowId, stopAndNotifyPKError) => {
  return await dataCollectionRepository.createReference(dataflowId, stopAndNotifyPKError);
};

const update = async dataflowId => {
  return await dataCollectionRepository.update(dataflowId);
};

export const DataCollectionService = {
  create,
  createReference,
  update
};
