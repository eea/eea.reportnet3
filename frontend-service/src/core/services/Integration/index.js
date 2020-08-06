import { All } from './All';
import { AllExtensionsOperations } from './AllExtensionsOperations';
import { Create } from './Create';
import { Delete } from './Delete';
import { FindEUDatasetIntegration } from './FindEUDatasetIntegration';
import { GetProcesses } from './GetProcesses';
import { GetRepositories } from './GetRepositories';
import { Update } from './Update';

import { integrationRepository } from 'core/domain/model/Integration/IntegrationRepository';

export const IntegrationService = {
  all: All({ integrationRepository }),
  allExtensionsOperations: AllExtensionsOperations({ integrationRepository }),
  create: Create({ integrationRepository }),
  deleteById: Delete({ integrationRepository }),
  findEUDatasetIntegration: FindEUDatasetIntegration({ integrationRepository }),
  getProcesses: GetProcesses({ integrationRepository }),
  getRepositories: GetRepositories({ integrationRepository }),
  update: Update({ integrationRepository })
};
