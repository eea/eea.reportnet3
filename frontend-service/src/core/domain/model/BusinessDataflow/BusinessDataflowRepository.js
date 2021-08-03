import { ApiBusinessDataflowRepository } from 'core/infrastructure/domain/model/BusinessDataflow/ApiBusinessDataflowRepository';

export const BusinessDataflowRepository = {
  all: () => Promise.reject('[BusinessDataflowRepository#all] must be implemented'),
  create: () => Promise.reject('[BusinessDataflowRepository#create] must be implemented'),
  update: () => Promise.reject('[BusinessDataflowRepository#update] must be implemented')
};

export const businessDataflowRepository = Object.assign({}, BusinessDataflowRepository, ApiBusinessDataflowRepository);
