export const UpdateTableDescriptionDesign = ({ datasetRepository }) => async (
  tableSchemaCopyTableData,
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId
) =>
  datasetRepository.updateTableDescriptionDesign(
    tableSchemaCopyTableData,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId
  );
