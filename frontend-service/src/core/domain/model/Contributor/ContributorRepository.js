import { ApiContributorRepository } from 'core/infrastructure/domain/model/Contributor/ApiContributorRepository';

export const ContributorRepository = {
  allEditors: () => Promise.reject('[ContributorRepository#allEditors] must be implemented'),
  allReporters: () => Promise.reject('[ContributorRepository#allReporters] must be implemented'),
  deleteEditor: () => Promise.reject('[ContributorRepository#deleteEditor] must be implemented'),
  deleteReporter: () => Promise.reject('[ContributorRepository#deleteReporter] must be implemented'),
  updateEditor: () => Promise.reject('[ContributorRepository#updateEditor] must be implemented'),
  updateReporter: () => Promise.reject('[ContributorRepository#updateReporter] must be implemented')
};

export const contributorRepository = Object.assign({}, ContributorRepository, ApiContributorRepository);
