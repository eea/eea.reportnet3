import isNil from 'lodash/isNil';

import { apiRelease } from 'core/infrastructure/api/domain/model/Release/ApiRelease';
import { Release } from 'core/domain/model/Release/Release';

const allHistoricReleases = async datasetId => {
  const releasesDTO = await apiRelease.allHistoricReleases(datasetId);
  return parseReleases(releasesDTO);
};

const allRepresentativeHistoricReleases = async (dataflowId, dataProviderId) => {
  const releasesDTO = await apiRelease.allRepresentativeHistoricReleases(dataflowId, dataProviderId);
  return parseReleases(releasesDTO);
};

const parseReleases = releasesDTO => {
  if (!isNil(releasesDTO)) {
    return releasesDTO.map(
      releaseDTO =>
        new Release({
          id: releaseDTO.id,
          countryCode: releaseDTO.countryCode,
          datasetId: releaseDTO.datasetId,
          datasetName: releaseDTO.datasetName,
          isDataCollectionReleased: releaseDTO.dcrelease,
          isEUReleased: releaseDTO.eurelease,
          releasedDate: releaseDTO.dateReleased
        })
    );
  }
  return;
};

export const ApiReleaseRepository = {
  allHistoricReleases,
  allRepresentativeHistoricReleases
};
