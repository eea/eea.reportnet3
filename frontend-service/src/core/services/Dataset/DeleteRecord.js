export const DeleteRecord = ({ datasetRepository }) => async (datasetId, recordId) =>
  datasetRepository.deleteRecordById(datasetId, recordId);
