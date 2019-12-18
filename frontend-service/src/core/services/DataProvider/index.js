import { Add } from './Add';
import { Delete } from './Delete';
import { AllRepresentatives } from './AllRepresentatives';
import { AllDataProviders } from './AllDataProviders';
import { Update } from './Update';

import { dataProviderRepository } from 'core/domain/model/DataProvider/DataProviderRepository';

export const DataProviderService = {
  allRepresentatives: AllRepresentatives({ dataProviderRepository }),
  allDataProviders: AllDataProviders({ dataProviderRepository }),
  add: Add({ dataProviderRepository }),
  delete: Delete({ dataProviderRepository }),
  update: Update({ dataProviderRepository })
};
