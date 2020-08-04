export const DownloadExportFile = ({ datasetRepository }) => async (datasetId, providerId) =>
  datasetRepository.downloadExportFile(datasetId, providerId);
