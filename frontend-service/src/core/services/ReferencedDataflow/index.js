import { referencedDataflowRepository } from 'core/domain/model/ReferencedDataflow/ReferencedDataflowRepository';

import { Create } from './Create';
import { GetAll } from './GetAll';

export const ReferenceDataflowService = {
  all: GetAll({ referencedDataflowRepository }),
  create: Create({ referencedDataflowRepository })
};
