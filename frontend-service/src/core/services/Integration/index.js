import { All } from './All';
import { AllExtensionsOperations } from './AllExtensionsOperations';
import { Create } from './Create';
import { Delete } from './Delete';
import { Update } from './Update';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository }),
  allExtensionsOperations: AllExtensionsOperations({ integrationRepository }),
  create: Create({ integrationRepository }),
  deleteById: Delete({ integrationRepository }),
  update: Update({ integrationRepository })
};
