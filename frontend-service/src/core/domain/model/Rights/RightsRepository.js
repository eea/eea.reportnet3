import { ApiRightsRepository } from 'core/infrastructure/domain/model/Rights/ApiRightsRepository';

export const RightsRepository = {
  allEditors: () => Promise.reject('[RightsRepository#allEditors] must be implemented'),
  allRequesters: () => Promise.reject('[RightsRepository#allRequesters] must be implemented'),
  allReporters: () => Promise.reject('[RightsRepository#allReporters] must be implemented'),
  deleteEditor: () => Promise.reject('[RightsRepository#deleteEditor] must be implemented'),
  deleteRequester: () => Promise.reject('[RightsRepository#deleteRequester] must be implemented'),
  deleteReporter: () => Promise.reject('[RightsRepository#deleteReporter] must be implemented'),
  updateEditor: () => Promise.reject('[RightsRepository#updateEditor] must be implemented'),
  updateReporter: () => Promise.reject('[RightsRepository#updateReporter] must be implemented'),
  updateRequester: () => Promise.reject('[RightsRepository#updateRequester] must be implemented')
};

export const rightsRepository = Object.assign({}, RightsRepository, ApiRightsRepository);
