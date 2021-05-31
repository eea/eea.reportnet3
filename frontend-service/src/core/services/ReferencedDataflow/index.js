import { referencedDataflowRepository } from 'core/domain/model/ReferencedDataflow/ReferencedDataflowRepository';

import { GetAll } from './GetAll';

export const ReferenceDataflowService = {
  all: GetAll({ referencedDataflowRepository })
};
