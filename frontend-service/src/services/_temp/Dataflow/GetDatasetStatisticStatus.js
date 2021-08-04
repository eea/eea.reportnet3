export const GetDatasetStatisticStatus = ({ dataflowRepository }) => async (dataflowId, datasetSchemaId) =>
  dataflowRepository.datasetsValidationStatistics(dataflowId, datasetSchemaId);
