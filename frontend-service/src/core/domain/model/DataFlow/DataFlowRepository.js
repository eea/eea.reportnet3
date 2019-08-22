import { ApiDataFlowRepository } from 'core/infrastructure/domain/model/DataFlow/ApiDataFlowRepository';

export const DataFlowRepository = {
  all: () => Promise.reject('[DataFlowRepository#all] must be implemented')
};

export const dataFlowRepository = Object.assign({}, DataFlowRepository, ApiDataFlowRepository);
