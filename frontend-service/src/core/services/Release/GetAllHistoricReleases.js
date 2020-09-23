export const GetAllHistoricReleases = ({ releaseRepository }) => async datasetId =>
  releaseRepository.allHistoricReleases(datasetId);
