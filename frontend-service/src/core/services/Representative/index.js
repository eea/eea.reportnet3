import { Add } from './Add';
import { AddLeadReporter } from './AddLeadReporter';
import { AllDataProviders } from './AllDataProviders';
import { AllRepresentatives } from './AllRepresentatives';
import { Delete } from './Delete';
import { DeleteLeadReporter } from './DeleteLeadReporter';
import { GetProviderTypes } from './GetProviderTypes';
import { Update } from './Update';
import { UpdateDataProviderId } from './UpdateDataProviderId';
import { UpdateLeadReporter } from './UpdateLeadReporter';
import { UpdateProviderAccount } from './UpdateProviderAccount';

import { representativeRepository } from 'core/domain/model/Representative/RepresentativeRepository';

export const RepresentativeService = {
  add: Add({ representativeRepository }),
  addLeadReporter: AddLeadReporter({ representativeRepository }),
  allDataProviders: AllDataProviders({ representativeRepository }),
  allRepresentatives: AllRepresentatives({ representativeRepository }),
  deleteById: Delete({ representativeRepository }),
  deleteLeadReporter: DeleteLeadReporter({ representativeRepository }),
  getProviderTypes: GetProviderTypes({ representativeRepository }),
  update: Update({ representativeRepository }),
  updateDataProviderId: UpdateDataProviderId({ representativeRepository }),
  updateLeadReporter: UpdateLeadReporter({ representativeRepository }),
  updateProviderAccount: UpdateProviderAccount({ representativeRepository })
};
