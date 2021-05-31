import { ApiReferenceDataflowRepository } from 'core/infrastructure/domain/model/ReferencedDataflow/ApiReferenceDataflowRepository';

export const ReferencedDataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented')
};

export const referencedDataflowRepository = Object.assign(
  {},
  ReferencedDataflowRepository,
  ApiReferenceDataflowRepository
);
