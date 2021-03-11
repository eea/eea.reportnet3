import isNil from 'lodash/isNil';

import { apiHistoricRelease } from 'core/infrastructure/api/domain/model/HistoricRelease/ApiHistoricRelease';
import { HistoricRelease } from 'core/domain/model/HistoricRelease/HistoricRelease';

const allHistoricReleases = async datasetId => {
  const response = await apiHistoricRelease.allHistoricReleases(datasetId);
  response.data = parseReleases(response.data);

  return response;
};

const allRepresentativeHistoricReleases = async (dataflowId, dataProviderId) => {
  const response = await apiHistoricRelease.allRepresentativeHistoricReleases(dataflowId, dataProviderId);
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
          releasedDate: historicReleaseDTO.dateReleased
        })
    );
  }
  return;
};

export const ApiHistoricReleaseRepository = { allHistoricReleases, allRepresentativeHistoricReleases };
