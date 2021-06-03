export const DownloadFileData = ({ datasetRepository }) => async (dataflowId, datasetId, fieldId, providerId) =>
  datasetRepository.downloadFileData(dataflowId, datasetId, fieldId, providerId);
