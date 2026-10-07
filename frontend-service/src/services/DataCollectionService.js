import { DataCollectionRepository } from 'repositories/DataCollectionRepository';

export const DataCollectionService = {
  create: async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo, disableRulesEventChoice) =>
    await DataCollectionRepository.create(
      dataflowId,
      endDate,
      isManualTechnicalAcceptance,
      stopAndNotifySQLErrors,
      showPublicInfo,
      disableRulesEventChoice
    ),

  createReference: async (dataflowId, stopAndNotifyPKError) =>
    await DataCollectionRepository.createReference(dataflowId, stopAndNotifyPKError),

  update: async dataflowId => await DataCollectionRepository.update(dataflowId)
};
