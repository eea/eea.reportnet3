import { ApiValidationRepository } from 'core/infrastructure/domain/model/Validation/ApiValidationRepository';

export const ValidationRepository = {
  deleteById: () => Promise.reject('[DataCollectionRepository#deleteById] must be implemented')
};

export const validationRepository = Object.assign({}, ValidationRepository, ApiValidationRepository);
