import { All } from './All';
import { Create } from './Create';
import { Delete } from './Delete';
import { Update } from './Update';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository }),
  create: Create({ integrationRepository }),
  deleteById: Delete({ integrationRepository }),
  update: Update({ integrationRepository })
};
