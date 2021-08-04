export const DownloadExportDatasetFile = ({ datasetRepository }) => async (datasetId, fileName) =>
  datasetRepository.downloadExportDatasetFile(datasetId, fileName);
