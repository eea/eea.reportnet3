export const UpdateTableDescriptionDesign = ({ datasetRepository }) => async (
  tableSchemaToPrefill,
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId
) =>
  datasetRepository.updateTableDescriptionDesign(
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId
  );
