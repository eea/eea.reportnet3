import { GetAllHistoricReleases } from './GetAllHistoricReleases';
import { GetAllRepresentativeHistoricReleases } from './GetAllRepresentativeHistoricReleases';

import { historicReleaseRepository } from 'entities/HistoricRelease/HistoricReleaseRepository';

export const HistoricReleaseService = {
  allHistoricReleases: GetAllHistoricReleases({ historicReleaseRepository }),
  allRepresentativeHistoricReleases: GetAllRepresentativeHistoricReleases({ historicReleaseRepository })
};
