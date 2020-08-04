export const DownloadExportFile = ({ datasetRepository }) => async (datasetSchemaId, providerId) =>
  datasetRepository.downloadExportFile(datasetSchemaId, providerId);
