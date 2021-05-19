export const DownloadReferenceDatasetFileData = ({ datasetRepository }) => async (dataflowId, fileName) =>
  datasetRepository.downloadReferenceDatasetFileData(dataflowId, fileName);
