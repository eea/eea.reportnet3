export const GetStatistics = ({ datasetRepository }) => async datasetId =>
  datasetRepository.errorStatisticsById(datasetId);
