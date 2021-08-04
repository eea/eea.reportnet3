import { ApiIntegrationRepository } from 'repositories/_temp/model/Integration/ApiIntegrationRepository';

export const IntegrationRepository = {
  all: () => Promise.reject('[IntegrationRepository#all] must be implemented'),
  create: () => Promise.reject('[IntegrationRepository#create] must be implemented'),
  deleteById: () => Promise.reject('[IntegrationRepository#deleteById] must be implemented'),
  findEUDatasetIntegration: () =>
    Promise.reject('[IntegrationRepository#findEUDatasetIntegration] must be implemented'),
  getProcesses: () => Promise.reject('[IntegrationRepository#getProcesses] must be implemented'),
  getRepositories: () => Promise.reject('[IntegrationRepository#getRepositories] must be implemented'),
  update: () => Promise.reject('[IntegrationRepository#update] must be implemented')
};

export const integrationRepository = Object.assign({}, IntegrationRepository, ApiIntegrationRepository);
