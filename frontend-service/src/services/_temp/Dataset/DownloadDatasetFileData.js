export const DownloadDatasetFileData = ({ datasetRepository }) => async (dataflowId, dataProviderId, fileName) =>
  datasetRepository.downloadDatasetFileData(dataflowId, dataProviderId, fileName);
