import { ApiHistoricReleaseRepository } from 'repositories/_temp/model/HistoricRelease/ApiHistoricReleaseRepository';

export const HistoricReleaseRepository = {
  allHistoricReleases: () => Promise.reject('[HisotricReleaseRepository#createById] must be implemented'),
  allRepresentativeHistoricReleases: () => Promise.reject('[HisotricReleaseRepository#createById] must be implemented')
};

export const historicReleaseRepository = Object.assign({}, HistoricReleaseRepository, ApiHistoricReleaseRepository);
