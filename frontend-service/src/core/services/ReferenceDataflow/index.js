import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { Create } from './Create';
import { GetAll } from './GetAll';
import { GetReferenceDataflow } from './GetReferenceDataflow';
import { GetReferencingDataflows } from './GetReferencingDataflows';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository }),
  create: Create({ referenceDataflowRepository }),
  getReferencingDataflows: GetReferencingDataflows({ referenceDataflowRepository }),
  referenceDataflow: GetReferenceDataflow({ referenceDataflowRepository })
};
