export const UpdateTableDescriptionDesign = ({ datasetRepository }) => async (
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId
) =>
  datasetRepository.updateTableDescriptionDesign(
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId
  );
