import { Add } from './Add';
import { AllRepresentatives } from './AllRepresentatives';
import { AllDataProviders } from './AllDataProviders';
import { Delete } from './Delete';
import { GetProviderTypes } from './GetProviderTypes';
import { Update } from './Update';
import { UpdateDataProviderId } from './UpdateDataProviderId';
import { UpdateProviderAccount } from './UpdateProviderAccount';

import { representativeRepository } from 'core/domain/model/Representative/RepresentativeRepository';

export const RepresentativeService = {
  allRepresentatives: AllRepresentatives({ representativeRepository }),
  allDataProviders: AllDataProviders({ representativeRepository }),
  add: Add({ representativeRepository }),
  deleteById: Delete({ representativeRepository }),
  getProviderTypes: GetProviderTypes({ representativeRepository }),
  update: Update({ representativeRepository }),
  updateDataProviderId: UpdateDataProviderId({ representativeRepository }),
  updateProviderAccount: UpdateProviderAccount({ representativeRepository })
};
