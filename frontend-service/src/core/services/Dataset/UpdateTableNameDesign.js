export const UpdateTableNameDesign = ({ datasetRepository }) => async (tableSchemaId, tableSchemaName, datasetId) =>
  datasetRepository.UpdateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
