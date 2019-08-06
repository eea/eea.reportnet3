export const GetDataSetStatistics = ({ dataSetRepository }) => async dataSetId =>
  dataSetRepository.errorStatisticsById(dataSetId);
