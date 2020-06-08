import { ApiIntegrationRepository } from 'core/infrastructure/domain/model/Integration/ApiIntegrationRepository';

export const IntegrationRepository = {
  all: () => Promise.reject('[IntegrationRepository#all] must be implemented'),
  create: () => Promise.reject('[IntegrationRepository#create] must be implemented'),
  deleteById: () => Promise.reject('[IntegrationRepository#deleteById] must be implemented')
};

export const integrationRepository = Object.assign({}, IntegrationRepository, ApiIntegrationRepository);
