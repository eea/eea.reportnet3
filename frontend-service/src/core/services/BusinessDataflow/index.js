import { businessDataflowRepository } from 'core/domain/model/BusinessDataflow/BusinessDataflowRepository';

import { Create } from './Create';
import { Edit } from './Edit';
import { GetAll } from './GetAll';

export const BusinessDataflowService = {
  all: GetAll({ businessDataflowRepository }),
  create: Create({ businessDataflowRepository }),
  edit: Edit({ businessDataflowRepository })
};
