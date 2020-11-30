export const DeleteRecord = ({ datasetRepository }) => async (datasetId, recordId, deleteInCascade) =>
  datasetRepository.deleteRecordById(datasetId, recordId, deleteInCascade);
