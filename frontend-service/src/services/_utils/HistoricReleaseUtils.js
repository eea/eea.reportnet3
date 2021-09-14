import { HistoricRelease } from 'entities/HistoricRelease';

const parseHistoricReleaseListDTO = historicReleasesDTO =>
  historicReleasesDTO?.map(
    historicReleaseDTO =>
      new HistoricRelease({
        dataProviderCode: historicReleaseDTO.countryCode,
        datasetId: historicReleaseDTO.datasetId,
        datasetName: historicReleaseDTO.datasetName,
        id: historicReleaseDTO.id,
        isDataCollectionReleased: historicReleaseDTO.dcrelease,
        isEUReleased: historicReleaseDTO.eurelease,
        releaseDate: historicReleaseDTO.dateReleased
      })
  );

export const HistoricReleaseUtils = {
  parseHistoricReleaseListDTO
};
