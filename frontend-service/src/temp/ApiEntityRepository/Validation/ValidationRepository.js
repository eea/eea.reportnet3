import { ApiValidationRepository } from 'repositories/_temp/model/Validation/ApiValidationRepository';

export const ValidationRepository = {
  create: () => Promise.reject('[ValidationRepository#create] must be implemented'),
  createRowRule: () => Promise.reject('[ValidationRepository#createRowRule] must be implemented'),
  createTableRule: () => Promise.reject('[ValidationRepository#createTableRule] must be implemented'),
  deleteById: () => Promise.reject('[ValidationRepository#deleteById] must be implemented'),
  downloadFile: () => Promise.reject('[ValidationRepository#downloadFile] must be implemented'),
  generateFile: () => Promise.reject('[ValidationRepository#generateFile] must be implemented'),
  getAll: () => Promise.reject('[ValidationRepository#getAll] must be implemented'),
  update: () => Promise.reject('[ValidationRepository#update] must be implemented'),
  updateDatasetRule: () => Promise.reject('[ValidationRepository#updateDatasetRule] must be implemented'),
  updateRowRule: () => Promise.reject('[ValidationRepository#updateRowRule] must be implemented')
};

export const validationRepository = Object.assign({}, ValidationRepository, ApiValidationRepository);
