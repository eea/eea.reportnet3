import { apiRelease } from 'core/infrastructure/api/domain/model/Release/ApiRelease';
import { Release } from 'core/domain/model/Release/Release';

const allDataCollectionHistoricReleases = async datasetId => {
  const releasesDTO = await apiRelease.allDataCollectionHistoricReleases(datasetId);
  return releasesDTO
    ? releasesDTO.map(
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
      )
    : [];
};

export const ApiReleaseRepository = {
  allDataCollectionHistoricReleases
};
