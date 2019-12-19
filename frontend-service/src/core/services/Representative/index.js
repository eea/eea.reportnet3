import { Add } from './Add';
import { Delete } from './Delete';
import { AllRepresentatives } from './AllRepresentatives';
import { AllDataProviders } from './AllDataProviders';
import { Update } from './Update';

import { representativeRepository } from 'core/domain/model/Representative/RepresentativeRepository';

export const DataProviderService = {
  allRepresentatives: AllRepresentatives({ representativeRepository }),
  allDataProviders: AllDataProviders({ representativeRepository }),
  add: Add({ representativeRepository }),
  delete: Delete({ representativeRepository }),
  update: Update({ representativeRepository })
};
