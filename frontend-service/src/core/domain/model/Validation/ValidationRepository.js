import { ApiValidationRepository } from 'core/infrastructure/domain/model/Validation/ApiValidationRepository';

export const ValidationRepository = {};

export const validationRepository = Object.assign({}, ValidationRepository, ApiValidationRepository);
