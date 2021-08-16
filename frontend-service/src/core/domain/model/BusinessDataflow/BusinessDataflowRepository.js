import { ApiBusinessDataflowRepository } from 'core/infrastructure/domain/model/BusinessDataflow/ApiBusinessDataflowRepository';

export const BusinessDataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  create: () => Promise.reject('[DataflowRepository#create] must be implemented'),
  update: () => Promise.reject('[DataflowRepository#update] must be implemented')
};

export const businessDataflowRepository = Object.assign({}, BusinessDataflowRepository, ApiBusinessDataflowRepository);
