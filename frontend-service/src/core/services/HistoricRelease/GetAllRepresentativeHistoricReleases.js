export const GetAllRepresentativeHistoricReleases = ({ historicReleaseRepository }) => async (dataflowId, providerId) =>
  historicReleaseRepository.allRepresentativeHistoricReleases(dataflowId, providerId);
