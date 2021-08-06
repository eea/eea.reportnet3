import isNil from 'lodash/isNil';

import { HistoricReleaseRepository } from 'repositories/HistoricReleaseRepository';
import { HistoricRelease } from 'entities/HistoricRelease';

const allHistoricReleases = async datasetId => {
  const response = await HistoricReleaseRepository.allHistoricReleases(datasetId);
  response.data = parseReleases(response.data);

  return response;
};

const allRepresentativeHistoricReleases = async (dataflowId, dataProviderId) => {
  const response = await HistoricReleaseRepository.allRepresentativeHistoricReleases(dataflowId, dataProviderId);
  response.data = parseReleases(response.data);

  return response;
};

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

export const HistoricReleaseService = { allHistoricReleases, allRepresentativeHistoricReleases };
