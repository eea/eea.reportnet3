import { ApiRepresentativeRepository } from 'repositories/_temp/model/Representative/ApiRepresentativeRepository';

export const RepresentativeRepository = {
  add: () => Promise.reject('[RepresentativeRepository#add] must be implemented'),
  addLeadReporter: () => Promise.reject('[RepresentativeRepository#addLeadReporter] must be implemented'),
  allDataProviders: () => Promise.reject('[RepresentativeRepository#allDataProviders] must be implemented'),
  allRepresentatives: () => Promise.reject('[RepresentativeRepository#allRepresentatives] must be implemented'),
  deleteById: () => Promise.reject('[RepresentativeRepository#deleteById] must be implemented'),
  deleteLeadReporter: () => Promise.reject('[RepresentativeRepository#deleteLeadReporter] must be implemented'),
  getFmeUsers: () => Promise.reject('[RepresentativeRepository#getFmeUsers] must be implemented'),
  getGroupCompanies: () => Promise.reject('[RepresentativeRepository#getGroupCompanies] must be implemented'),
  getGroupProviders: () => Promise.reject('[RepresentativeRepository#getGroupProviders] must be implemented'),
  update: () => Promise.reject('[RepresentativeRepository#updateById] must be implemented'),
  updateDataProviderId: () => Promise.reject('[RepresentativeRepository#updateDataProviderId] must be implemented'),
  updateLeadReporter: () => Promise.reject('[RepresentativeRepository#updateLeadReporter] must be implemented')
};

export const representativeRepository = Object.assign({}, RepresentativeRepository, ApiRepresentativeRepository);
