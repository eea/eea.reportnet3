export const ExportTableSchema = ({ datasetRepository }) => async (datasetId, datasetSchemaId, tableSchemaId, fileType) =>
  datasetRepository.exportTableSchemaById(datasetId, datasetSchemaId, tableSchemaId, fileType);
