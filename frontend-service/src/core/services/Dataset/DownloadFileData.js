export const DownloadFileData = ({ datasetRepository }) => async (dataflowId, datasetId, fieldId, dataProviderId) =>
  datasetRepository.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);
