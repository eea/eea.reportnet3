import isNil from 'lodash/isNil';

import { HistoricReleaseRepository } from 'repositories/HistoricReleaseRepository';
import { HistoricRelease } from 'entities/HistoricRelease';

const parseReleases = historicReleasesDTO => {
  if (!isNil(historicReleasesDTO)) {
    return historicReleasesDTO.map(
      historicReleaseDTO =>
        new HistoricRelease({
          countryCode: historicReleaseDTO.countryCode,
          datasetId: historicReleaseDTO.datasetId,
          datasetName: historicReleaseDTO.datasetName,
          id: historicReleaseDTO.id,
          isDataCollectionReleased: historicReleaseDTO.dcrelease,
          isEUReleased: historicReleaseDTO.eurelease,
          releaseDate: historicReleaseDTO.dateReleased
        })
    );
  }
  return;
};

export const HistoricReleaseService = {
  getAll: async datasetId => {
    const response = await HistoricReleaseRepository.getAll(datasetId);
    return parseReleases(response.data);
  },

  getAllRepresentative: async (dataflowId, dataProviderId) => {
    const response = await HistoricReleaseRepository.getAllRepresentative(dataflowId, dataProviderId);
    return parseReleases(response.data);
  }
};
