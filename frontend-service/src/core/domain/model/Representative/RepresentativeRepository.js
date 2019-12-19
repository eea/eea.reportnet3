import { ApiRepresentativeRepository } from 'core/infrastructure/domain/model/Representative/ApiRepresentativeRepository';

export const RepresentativeRepository = {
  allRepresentatives: () => Promise.reject('[RepresentativeRepository#all] must be implemented'),
  allRepresentatives: () => Promise.reject('[RepresentativeRepository#allRepresentatives] must be implemented'),
  add: () => Promise.reject('[RepresentativeRepository#addByLogin] must be implemented'),
  deleteById: () => Promise.reject('[RepresentativeRepository#deleteById] must be implemented'),
  update: () => Promise.reject('[RepresentativeRepository#updateById] must be implemented')
};

export const representativeRepository = Object.assign({}, RepresentativeRepository, ApiRepresentativeRepository);
