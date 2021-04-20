import { ApiUserRightRepository } from 'core/infrastructure/domain/model/UserRight/ApiUserRightRepository';

export const UserRightRepository = {
  allEditors: () => Promise.reject('[UserRightRepository#allEditors] must be implemented'),
  allReporters: () => Promise.reject('[UserRightRepository#allReporters] must be implemented'),
  allRequesters: () => Promise.reject('[UserRightRepository#allRequesters] must be implemented'),
  deleteEditor: () => Promise.reject('[UserRightRepository#deleteEditor] must be implemented'),
  deleteReporter: () => Promise.reject('[UserRightRepository#deleteReporter] must be implemented'),
  deleteRequester: () => Promise.reject('[UserRightRepository#deleteRequester] must be implemented'),
  updateEditor: () => Promise.reject('[UserRightRepository#updateEditor] must be implemented'),
  updateReporter: () => Promise.reject('[UserRightRepository#updateReporter] must be implemented'),
  updateRequester: () => Promise.reject('[UserRightRepository#updateRequester] must be implemented')
};

export const userRightRepository = Object.assign({}, UserRightRepository, ApiUserRightRepository);
