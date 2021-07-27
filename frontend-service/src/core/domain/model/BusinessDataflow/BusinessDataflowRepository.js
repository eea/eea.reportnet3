import { ApiBusinessDataflowRepository } from 'core/infrastructure/domain/model/Dataflow/ApiBusinessDataflowRepository';

export const DataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  create: () => Promise.reject('[DataflowRepository#create] must be implemented'),
  update: () => Promise.reject('[DataflowRepository#update] must be implemented')
};

export const dataflowRepository = Object.assign({}, DataflowRepository, ApiBusinessDataflowRepository);
