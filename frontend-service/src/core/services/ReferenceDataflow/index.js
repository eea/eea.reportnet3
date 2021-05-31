import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { Create } from './Create';
import { GetAll } from './GetAll';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository }),
  create: Create({ referenceDataflowRepository })
};
