export const Create = ({ dataCollectionRepository }) => async (
  dataflowId,
  endDate,
  isManualTechnicalAcceptance,
  stopAndNotifySQLErrors
) => dataCollectionRepository.create(dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors);
