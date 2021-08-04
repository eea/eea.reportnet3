export const GetStatistics = ({ datasetRepository }) => async (datasetId, tableSchemaNames) =>
  datasetRepository.errorStatisticsById(datasetId, tableSchemaNames);
