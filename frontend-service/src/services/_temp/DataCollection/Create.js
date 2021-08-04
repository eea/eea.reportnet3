export const Create = ({ dataCollectionRepository }) => async (
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
