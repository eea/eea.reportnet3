export const UpdateTableNameDesign = ({ datasetRepository }) => async (tableSchemaId, tableSchemaName, datasetId) =>
  datasetRepository.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
