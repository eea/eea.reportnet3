export const Update = ({ uniqueConstraintsRepository }) => async (
  datasetSchemaId,
  fieldSchemaIds,
  tableSchemaId,
  uniqueId
) => uniqueConstraintsRepository.update(datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
