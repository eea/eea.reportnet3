import { dataCollectionRepository } from 'core/domain/model/DataCollection/DataCollectionRepository';

import { Create } from './Create';
import { CreateReference } from './CreateReference';
import { Update } from './Update';

export const DataCollectionService = {
  create: Create({ dataCollectionRepository }),
  createReference: CreateReference({ dataCollectionRepository }),
  update: Update({ dataCollectionRepository })
};
