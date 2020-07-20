export const CopyDataCollection = ({ euDatasetRepository }) => async (dataflowId, endDate) =>
  euDatasetRepository.copyDataCollection(dataflowId, endDate);
