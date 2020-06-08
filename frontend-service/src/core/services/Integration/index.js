import { All } from './All';
import { Create } from './Create';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository }),
  create: Create({ integrationRepository })
};
