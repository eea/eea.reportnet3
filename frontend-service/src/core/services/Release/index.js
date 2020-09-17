import { GetAllDataCollectionHistoricReleases } from './GetAllDataCollectionHistoricReleases';
import { releaseRepository } from 'core/domain/model/Release/ReleaseRepository';

export const ReleaseService = {
  allDataCollectionHistoricReleases: GetAllDataCollectionHistoricReleases({ releaseRepository })
};
