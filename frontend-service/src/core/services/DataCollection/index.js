import { dataCollectionRepository } from 'core/domain/model/DataCollection/DataCollectionRepository';

import { Create } from './Create';
import { Update } from './Update';

export const DataCollectionService = {
  create: Create({ dataCollectionRepository }),
  update: Update({ dataCollectionRepository })
};
