import { ApiReleaseRepository } from 'core/infrastructure/domain/model/Release/ApiReleaseRepository';

export const ReleaseRepository = {
  allDataCollectionHistoricReleases: () =>
    Promise.reject('[ReleaseDatasetDesignerRepository#createById] must be implemented')
};

export const releaseRepository = Object.assign({}, ReleaseRepository, ApiReleaseRepository);
