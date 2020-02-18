import { Delete } from './Delete';

import { validationRepository } from 'core/domain/model/Validation/ValidationRepository';

export const ValidationService = {
  deleteById: Delete({ validationRepository })
};
