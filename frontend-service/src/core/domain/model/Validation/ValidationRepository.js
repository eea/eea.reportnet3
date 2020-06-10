import { ApiValidationRepository } from 'core/infrastructure/domain/model/Validation/ApiValidationRepository';

export const ValidationRepository = {
  create: () => Promise.reject('[ValidationRepository#create] must be implemented'),
  createDatasetRule: () => Promise.reject('[ValidationRepository#createDatasetRule] must be implemented'),
  createRowRule: () => Promise.reject('[ValidationRepository#createRowRule] must be implemented'),
  deleteById: () => Promise.reject('[ValidationRepository#deleteById] must be implemented'),
  getAll: () => Promise.reject('[ValidationRepository#getAll] must be implemented'),
  update: () => Promise.reject('[ValidationRepository#update] must be implemented'),
  updateRowRule: () => Promise.reject('[ValidationRepository#updateRowRule] must be implemented')
};

export const validationRepository = Object.assign({}, ValidationRepository, ApiValidationRepository);
