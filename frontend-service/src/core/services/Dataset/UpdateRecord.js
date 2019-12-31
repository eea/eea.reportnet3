export const UpdateRecord = ({ datasetRepository }) => async (datasetId, record) =>
  datasetRepository.updateRecordsById(datasetId, record);
