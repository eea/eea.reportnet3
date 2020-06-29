import { ApiContributorRepository } from 'core/infrastructure/domain/model/Contributor/ApiContributorRepository';

export const ContributorRepository = {
  add: () => Promise.reject('[ContributorRepository#add] must be implemented'),
  all: () => Promise.reject('[ContributorRepository#all] must be implemented'),
  deleteContributor: () => Promise.reject('[ContributorRepository#deleteContributor] must be implemented'),
  updateWritePermission: () => Promise.reject('[ContributorRepository#updateWritePermission] must be implemented')
};

export const contributorRepository = Object.assign({}, ContributorRepository, ApiContributorRepository);
