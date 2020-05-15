export const Delete = ({ uniqueConstraintsRepository }) => async (datasetSchemaId, constraintId) =>
  uniqueConstraintsRepository.deleteById(datasetSchemaId, constraintId);
