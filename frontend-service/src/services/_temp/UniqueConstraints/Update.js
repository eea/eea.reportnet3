export const Update = ({ uniqueConstraintsRepository }) => async (
  dataflowId,
  datasetSchemaId,
  fieldSchemaIds,
  tableSchemaId,
  uniqueId
) => uniqueConstraintsRepository.update(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
