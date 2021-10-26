import { HistoricRelease } from 'entities/HistoricRelease';

const parseHistoricReleaseListDTO = historicReleasesDTO =>
  historicReleasesDTO?.map(
    historicReleaseDTO =>
      new HistoricRelease({
        dataProviderCode: historicReleaseDTO.dataProviderCode,
        datasetId: historicReleaseDTO.datasetId,
        datasetName: historicReleaseDTO.datasetName,
        id: historicReleaseDTO.id,
        isDataCollectionReleased: historicReleaseDTO.dcrelease,
        isEUReleased: historicReleaseDTO.eurelease,
        releaseDate: historicReleaseDTO.dateReleased,
        restrictFromPublic: true
      })
  );

export const HistoricReleaseUtils = {
  parseHistoricReleaseListDTO
};
