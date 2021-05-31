import { referenceDataflowRepository } from 'core/domain/model/ReferenceDataflow/ReferenceDataflowRepository';

import { GetAll } from './GetAll';

export const ReferenceDataflowService = {
  all: GetAll({ referenceDataflowRepository })
};
