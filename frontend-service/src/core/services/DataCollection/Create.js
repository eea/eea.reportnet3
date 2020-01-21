export const Create = ({ dataCollectionRepository }) => async (dataflowId, endDate) =>
  dataCollectionRepository.create(dataflowId, endDate);
