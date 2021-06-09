import { ApiReferenceDataflowRepository } from 'core/infrastructure/domain/model/ReferenceDataflow/ApiReferenceDataflowRepository';

export const ReferenceDataflowRepository = {
  all: () => Promise.reject('[DataflowRepository#all] must be implemented'),
  create: () => Promise.reject('[DataflowRepository#create] must be implemented'),
  deleteReferenceDataflow: () => Promise.reject('[DataflowRepository#deleteReferenceDataflow] must be implemented'),
  edit: () => Promise.reject('[DataflowRepository#edit] must be implemented'),
  getReferencingDataflows: () => Promise.reject('[DataflowRepository#getReferencingDataflows] must be implemented'),
  referenceDataflow: () => Promise.reject('[DataflowRepository#referenceDataflow] must be implemented')
};

export const referenceDataflowRepository = Object.assign(
  {},
  ReferenceDataflowRepository,
  ApiReferenceDataflowRepository
);
