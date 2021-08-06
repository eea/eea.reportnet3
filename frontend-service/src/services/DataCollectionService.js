import { DataCollectionRepository } from 'repositories/DataCollectionRepository';

const create = async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) => {
  return await DataCollectionRepository.create(
    dataflowId,
    endDate,
    isManualTechnicalAcceptance,
    stopAndNotifySQLErrors,
    showPublicInfo
  );
};

const createReference = async (dataflowId, stopAndNotifyPKError) => {
  return await DataCollectionRepository.createReference(dataflowId, stopAndNotifyPKError);
};

const update = async dataflowId => {
  return await DataCollectionRepository.update(dataflowId);
};

export const DataCollectionService = {
  create,
  createReference,
  update
};
