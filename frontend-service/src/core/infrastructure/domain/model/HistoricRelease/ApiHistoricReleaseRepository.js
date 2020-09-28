import isNil from 'lodash/isNil';

import { apiHistoricRelease } from 'core/infrastructure/api/domain/model/HistoricRelease/ApiHistoricRelease';
import { HistoricRelease } from 'core/domain/model/HistoricRelease/HistoricRelease';

const allHistoricReleases = async datasetId => {
  const historicReleasesDTO = await apiHistoricRelease.allHistoricReleases(datasetId);
  return parseReleases(historicReleasesDTO);
};

const allRepresentativeHistoricReleases = async (dataflowId, dataProviderId) => {
  const historicReleasesDTO = await apiHistoricRelease.allRepresentativeHistoricReleases(dataflowId, dataProviderId);
  return parseReleases(historicReleasesDTO);
};

const parseReleases = historicReleasesDTO => {
  if (!isNil(historicReleasesDTO)) {
    return historicReleasesDTO.map(
      historicReleaseDTO =>
        new HistoricRelease({
          id: historicReleaseDTO.id,
          countryCode: historicReleaseDTO.countryCode,
          datasetId: historicReleaseDTO.datasetId,
          datasetName: historicReleaseDTO.datasetName,
          isDataCollectionReleased: historicReleaseDTO.dcrelease,
          isEUReleased: historicReleaseDTO.eurelease,
          releasedDate: historicReleaseDTO.dateReleased
        })
    );
  }
  return;
};

export const ApiHistoricReleaseRepository = {
  allHistoricReleases,
  allRepresentativeHistoricReleases
};
