export const All = ({ uniqueConstraintsRepository }) => async (dataflowId, datasetSchemaId) =>
  uniqueConstraintsRepository.all(dataflowId, datasetSchemaId);
