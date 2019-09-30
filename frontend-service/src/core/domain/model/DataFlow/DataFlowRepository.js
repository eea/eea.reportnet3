import { ApiDataflowRepository } from 'core/infrastructure/domain/model/DataFlow/ApiDataFlowRepository';

export const DataflowRepository = {
  accept: () => Promise.reject('[DataflowRepository#accept] must be implemented'),
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  accepted: () => Promise.reject('[DataflowRepository#accepted] must be implemented'),
  completed: () => Promise.reject('[DataflowRepository#completed] must be implemented'),
  dashboards: () => Promise.reject('[DataflowRepository#dashboards] must be implemented'),
  pending: () => Promise.reject('[DataflowRepository#pending] must be implemented'),
  reject: () => Promise.reject('[DataflowRepository#reject] must be implemented'),
  reporting: () => Promise.reject('[DataflowRepository#reporting] must be implemented')
};

export const dataflowRepository = Object.assign({}, DataflowRepository, ApiDataflowRepository);
