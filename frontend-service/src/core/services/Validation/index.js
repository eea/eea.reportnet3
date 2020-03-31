import { Create } from './Create';
import { Delete } from './Delete';
import { GetAll } from './GetAll';

import { validationRepository } from 'core/domain/model/Validation/ValidationRepository';

export const ValidationService = {
  deleteById: Delete({ validationRepository }),
  getAll: GetAll({ validationRepository }),
  create: Create({ validationRepository })
};
