export const Create = ({ uniqueConstraintsRepository }) => async (datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
  uniqueConstraintsRepository.create(datasetSchemaId, fieldSchemaIds, tableSchemaId);
