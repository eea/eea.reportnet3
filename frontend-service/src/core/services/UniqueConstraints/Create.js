export const Create = ({ uniqueConstraintsRepository }) => async (
  dataflowId,
  datasetSchemaId,
  fieldSchemaIds,
  tableSchemaId
) => uniqueConstraintsRepository.create(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId);
