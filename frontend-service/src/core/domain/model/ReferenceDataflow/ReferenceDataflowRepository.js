import { ApiReferenceDataflowRepository } from 'core/infrastructure/domain/model/ReferenceDataflow/ApiReferenceDataflowRepository';

export const ReferenceDataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  referenceDataflow: () => Promise.reject('[DataflowRepository#referenceDataflow] must be implemented')
};

export const referenceDataflowRepository = Object.assign(
  {},
  ReferenceDataflowRepository,
  ApiReferenceDataflowRepository
);
