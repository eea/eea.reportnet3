import { GetAllHistoricReleases } from './GetAllHistoricReleases';
import { GetAllRepresentativeHistoricReleases } from './GetAllRepresentativeHistoricReleases';

import { historicReleaseRepository } from 'core/domain/model/HistoricRelease/HistoricReleaseRepository';

export const HistoricReleaseService = {
  allHistoricReleases: GetAllHistoricReleases({ historicReleaseRepository }),
  allRepresentativeHistoricReleases: GetAllRepresentativeHistoricReleases({ historicReleaseRepository })
};
