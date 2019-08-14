import { ApiDataFlowRepository } from 'core/infrastructure/domain/model/DataFlow/ApiDataFlowRepository';

export const DataFlowRepository = {
  pending: () => Promise.reject('[DataFlowRepository#pending] must be implemented'),
  accepted: () => Promise.reject('[DataFlowRepository#accepted] must be implemented'),
  completed: () => Promise.reject('[DataFlowRepository#completed] must be implemented'),
  reporting: () => Promise.reject('[DataFlowRepository#reporting] must be implemented')
};

export const dataFlowRepository = Object.assign({}, DataFlowRepository, ApiDataFlowRepository);
