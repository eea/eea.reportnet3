export const UpdateTableDescriptionDesign = ({ datasetRepository }) => async (
  tableSchemaToPrefill,
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId,
  tableSchemaNotEmpty,
  tableSchemaFixedNumber
) =>
  datasetRepository.updateTableDescriptionDesign(
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
  );
