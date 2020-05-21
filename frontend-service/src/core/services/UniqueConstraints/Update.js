export const Update = ({ uniqueConstraintsRepository }) => async (
  datasetSchemaId,
  fieldSchemaIds,
  tableSchemaId,
  uniqueID
) => uniqueConstraintsRepository.update(datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueID);
