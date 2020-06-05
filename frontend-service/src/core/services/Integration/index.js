import { All } from './All';
import { Delete } from './Delete';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository }),
  deleteById: Delete({ integrationRepository })
};
