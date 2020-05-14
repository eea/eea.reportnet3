export const All = ({ uniqueConstraintsRepository }) => async datasetSchemaId =>
  uniqueConstraintsRepository.all(datasetSchemaId);
