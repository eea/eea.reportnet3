import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { Create } from './Create';
import { DeleteDataflow } from './DeleteDataflow';
import { Edit } from './Edit';
import { GetAll } from './GetAll';
import { GetReferenceDataflow } from './GetReferenceDataflow';
import { GetReferencingDataflows } from './GetReferencingDataflows';
import { ToggleUpdatable } from './toggleUpdatable';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository }),
  create: Create({ referenceDataflowRepository }),
  deleteReferenceDataflow: DeleteDataflow({ referenceDataflowRepository }),
  edit: Edit({ referenceDataflowRepository }),
  getReferencingDataflows: GetReferencingDataflows({ referenceDataflowRepository }),
  referenceDataflow: GetReferenceDataflow({ referenceDataflowRepository }),
  toggleUpdatable: ToggleUpdatable({ referenceDataflowRepository })
};
