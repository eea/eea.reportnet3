import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { GetAll } from './GetAll';
import { GetReferenceDataflow } from './GetReferenceDataflow';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository }),
  referenceDataflow: GetReferenceDataflow({ referenceDataflowRepository })
};
