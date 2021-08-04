export const DownloadExportFile = ({ datasetRepository }) => async (datasetId, fileName, providerId) =>
  datasetRepository.downloadExportFile(datasetId, fileName, providerId);
