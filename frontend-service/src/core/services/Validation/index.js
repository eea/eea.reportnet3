import { Create } from './Create';
import { CreateDatasetRule } from './CreateDatasetRule';
import { CreateRowRule } from './CreateRowRule';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';
import { UpdateRowRule } from './UpdateRowRule';

import { validationRepository } from 'core/domain/model/Validation/ValidationRepository';

export const ValidationService = {
  create: Create({ validationRepository }),
  createDatasetRule: CreateDatasetRule({ validationRepository }),
  createRowRule: CreateRowRule({ validationRepository }),
  deleteById: Delete({ validationRepository }),
  getAll: GetAll({ validationRepository }),
  update: Update({ validationRepository }),
  updateRowRule: UpdateRowRule({ validationRepository })
};
