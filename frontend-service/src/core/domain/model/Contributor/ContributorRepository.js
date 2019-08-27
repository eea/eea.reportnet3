import { ApiContributorRepository } from 'core/infrastructure/domain/model/Contributor/ApiContributorRepository';

export const ContributorRepository = {
  all: () => Promise.reject('[ContributorRepository#all] must be implemented'),
  addByLogin: () => Promise.reject('[ContributorRepository#addByLogin] must be implemented'),
  deleteById: () => Promise.reject('[ContributorRepository#deleteById] must be implemented'),
  updateById: () => Promise.reject('[ContributorRepository#updateById] must be implemented')
};

export const contributorRepository = Object.assign({}, ContributorRepository, ApiContributorRepository);
