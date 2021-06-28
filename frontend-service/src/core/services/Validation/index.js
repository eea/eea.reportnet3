import { Create } from './Create';
import { CreateRowRule } from './CreateRowRule';
import { CreateTableRule } from './CreateTableRule';
import { Delete } from './Delete';
import { DownloadFile } from './DownloadFile';
import { GenerateFile } from './GenerateFile';
import { GetAll } from './GetAll';
import { Update } from './Update';
import { UpdateDatasetRule } from './UpdateDatasetRule';
import { UpdateRowRule } from './UpdateRowRule';

import { validationRepository } from 'core/domain/model/Validation/ValidationRepository';

export const ValidationService = {
  create: Create({ validationRepository }),
  createRowRule: CreateRowRule({ validationRepository }),
  createTableRule: CreateTableRule({ validationRepository }),
  deleteById: Delete({ validationRepository }),
  downloadFile: DownloadFile({ validationRepository }),
  generateFile: GenerateFile({ validationRepository }),
  getAll: GetAll({ validationRepository }),
  update: Update({ validationRepository }),
  updateDatasetRule: UpdateDatasetRule({ validationRepository }),
  updateRowRule: UpdateRowRule({ validationRepository })
};
