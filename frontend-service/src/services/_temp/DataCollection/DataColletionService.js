//import { dataCollectionRepository } from 'entities/DataCollection/DataCollectionRepository';

const Create = ({ dataCollectionRepository }) => async (
  dataflowId,
  endDate,
  isManualTechnicalAcceptance,
  stopAndNotifySQLErrors,
  showPublicInfo
) =>
  dataCollectionRepository.create(
    dataflowId,
    endDate,
    isManualTechnicalAcceptance,
    stopAndNotifySQLErrors,
    showPublicInfo
  );




export const DataCollectionService = {
  create: Create({ dataCollectionRepository }),
  createReference: CreateReference({ dataCollectionRepository }),
  update: Update({ dataCollectionRepository })
};
