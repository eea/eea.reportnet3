export const GetAllRepresentativeHistoricReleases = ({ releaseRepository }) => async (dataflowId, providerId) =>
  releaseRepository.allRepresentativeHistoricReleases(dataflowId, providerId);
