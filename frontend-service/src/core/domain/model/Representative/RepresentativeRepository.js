import { ApiRepresentativeRepository } from 'core/infrastructure/domain/model/Representative/ApiRepresentativeRepository';

export const RepresentativeRepository = {
  allDataProviders: () => Promise.reject('[RepresentativeRepository#allDataProviders] must be implemented'),
  allRepresentatives: () => Promise.reject('[RepresentativeRepository#allRepresentatives] must be implemented'),
  add: () => Promise.reject('[RepresentativeRepository#add] must be implemented'),
  deleteById: () => Promise.reject('[RepresentativeRepository#deleteById] must be implemented'),
  getProviderTypes: () => Promise.reject('[RepresentativeRepository#getProviderTypes] must be implemented'),
  update: () => Promise.reject('[RepresentativeRepository#updateById] must be implemented'),
  updateProviderAccount: () => Promise.reject('[RepresentativeRepository#updateProviderAccount] must be implemented'),
  updateDataProviderId: () => Promise.reject('[RepresentativeRepository#updateDataProviderId] must be implemented')
};

export const representativeRepository = Object.assign({}, RepresentativeRepository, ApiRepresentativeRepository);
