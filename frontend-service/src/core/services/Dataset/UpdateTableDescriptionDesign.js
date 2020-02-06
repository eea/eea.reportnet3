export const UpdateTableDescriptionDesign = ({ datasetRepository }) => async (
  tableSchemaId,
  tableSchemaDescription,
  datasetId
) => datasetRepository.updateTableDescriptionDesign(tableSchemaId, tableSchemaDescription, datasetId);
