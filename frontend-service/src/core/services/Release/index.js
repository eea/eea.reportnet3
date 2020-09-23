import { GetAllHistoricReleases } from './GetAllHistoricReleases';
import { GetAllRepresentativeHistoricReleases } from './GetAllRepresentativeHistoricReleases';
import { releaseRepository } from 'core/domain/model/Release/ReleaseRepository';

export const ReleaseService = {
  allHistoricReleases: GetAllHistoricReleases({ releaseRepository }),
  allRepresentativeHistoricReleases: GetAllRepresentativeHistoricReleases({ releaseRepository })
};
