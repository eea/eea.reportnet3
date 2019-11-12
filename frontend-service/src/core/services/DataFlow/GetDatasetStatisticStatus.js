export const GetDatasetStatisticStatus = ({ dataflowRepository }) => async datasetSchemaId =>
  dataflowRepository.datasetsValidationStatistics(datasetSchemaId);
