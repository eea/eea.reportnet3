export const GetAllHistoricReleases = ({ historicReleaseRepository }) => async datasetId =>
  historicReleaseRepository.allHistoricReleases(datasetId);
