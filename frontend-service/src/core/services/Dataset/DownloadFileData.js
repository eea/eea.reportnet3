export const DownloadFileData = ({ datasetRepository }) => async (datasetId, fieldId) =>
  datasetRepository.downloadFileData(datasetId, fieldId);
