export const ExportTableData = ({ datasetRepository }) => async (datasetId, tableSchemaId, fileType) =>
  datasetRepository.exportTableDataById(datasetId, tableSchemaId, fileType);
