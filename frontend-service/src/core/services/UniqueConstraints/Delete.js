export const Delete = ({ uniqueConstraintsRepository }) => async (datasetSchemaId, fieldId) =>
  uniqueConstraintsRepository.deleteById(datasetSchemaId, fieldId);
