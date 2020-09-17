export const GetAllDataCollectionHistoricReleases = ({ releaseRepository }) => async datasetId =>
  releaseRepository.allDataCollectionHistoricReleases(datasetId);
