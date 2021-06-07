import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { Create } from './Create';
import { Edit } from './Edit';
import { GetAll } from './GetAll';
import { GetReferenceDataflow } from './GetReferenceDataflow';
import { GetReferencingDataflows } from './GetReferencingDataflows';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository }),
  create: Create({ referenceDataflowRepository }),
  edit: Edit({ referenceDataflowRepository }),
  getReferencingDataflows: GetReferencingDataflows({ referenceDataflowRepository }),
  referenceDataflow: GetReferenceDataflow({ referenceDataflowRepository })
};
