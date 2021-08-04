export const ExportDatasetDataExternal = ({ datasetRepository }) => async (datasetId, integrationId) =>
  datasetRepository.exportDatasetDataExternal(datasetId, integrationId);
