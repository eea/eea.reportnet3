import { ApiIntegrationRepository } from 'core/infrastructure/domain/model/Integration/ApiIntegrationRepository';

export const IntegrationRepository = {
  all: () => Promise.reject('[UniqueConstraintsRepository#all] must be implemented')
};

export const integrationRepository = Object.assign({}, IntegrationRepository, ApiIntegrationRepository);
