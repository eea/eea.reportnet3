export const UpdateTableDesign = ({ datasetRepository }) => async (
  tableSchemaId,
  tableSchemaName,
  tableSchemaDescription,
  datasetId
) => datasetRepository.updateTableDesign(tableSchemaId, tableSchemaName, tableSchemaDescription, datasetId);
