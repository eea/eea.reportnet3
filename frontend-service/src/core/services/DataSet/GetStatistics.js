export const GetStatistics = ({ dataSetRepository }) => async dataSetId =>
  dataSetRepository.errorStatisticsById(dataSetId);
