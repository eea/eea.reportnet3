export const CloneDatasetSchemas = ({ dataflowRepository }) => async (sourceDataflowId, targetDataflowId) =>
  dataflowRepository.cloneDatasetSchemas(sourceDataflowId, targetDataflowId);
