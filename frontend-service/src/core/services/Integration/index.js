import { All } from './All';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository })
};
