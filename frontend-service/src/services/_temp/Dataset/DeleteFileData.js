export const DeleteFileData = ({ datasetRepository }) => async (datasetId, fieldId) =>
  datasetRepository.deleteFileData(datasetId, fieldId);
