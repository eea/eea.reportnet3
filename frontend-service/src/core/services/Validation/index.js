import { Create } from './Create';
import { CreateRowRule } from './CreateRowRule';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';

import { validationRepository } from 'core/domain/model/Validation/ValidationRepository';

export const ValidationService = {
  create: Create({ validationRepository }),
  createRowRule: CreateRowRule({ validationRepository }),
  deleteById: Delete({ validationRepository }),
  getAll: GetAll({ validationRepository }),
  update: Update({ validationRepository })
};
