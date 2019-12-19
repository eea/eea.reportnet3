import { Add } from './Add';
import { AllRepresentatives } from './AllRepresentatives';
import { AllDataProviders } from './AllDataProviders';
import { Delete } from './Delete';
import { GetProviderTypes } from './GetProviderTypes';
import { Update } from './Update';

import { representativeRepository } from 'core/domain/model/Representative/RepresentativeRepository';

export const RepresentativeService = {
  allRepresentatives: AllRepresentatives({ representativeRepository }),
  allDataProviders: AllDataProviders({ representativeRepository }),
  add: Add({ representativeRepository }),
  delete: Delete({ representativeRepository }),
  getProviderTypes: GetProviderTypes({ representativeRepository }),
  update: Update({ representativeRepository })
};
