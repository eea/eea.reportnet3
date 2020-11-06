export const Create = ({ dataCollectionRepository }) => async (dataflowId, endDate, stopAndNotifySQLErrors) =>
  dataCollectionRepository.create(dataflowId, endDate, stopAndNotifySQLErrors);
