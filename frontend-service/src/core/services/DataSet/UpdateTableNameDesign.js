export const UpdateTableNameDesign = ({ datasetRepository }) => async (
  datasetSchemaId,
  tableSchemaId,
  tableSchemaName,
  datasetId
) => datasetRepository.updateTableNameDesign(datasetSchemaId, tableSchemaId, tableSchemaName, datasetId);
