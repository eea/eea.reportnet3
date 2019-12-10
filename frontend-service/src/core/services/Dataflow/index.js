import { Accept } from './Accept';
import { Create } from './Create';
import { CreateDatasetSchema } from './CreateDatasetSchema';
import { dataflowRepository } from 'core/domain/model/Dataflow/DataflowRepository';
import { Delete } from './Delete';
import { GetPending } from './GetPending';
import { GetAccepted } from './GetAccepted';
import { GetAll } from './GetAll';
import { GetCompleted } from './GetCompleted';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetDetails } from './GetDetails';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Reject } from './Reject';
import { Update } from './Update';

export const DataflowService = {
  accept: Accept({ dataflowRepository }),
  accepted: GetAccepted({ dataflowRepository }),
  all: GetAll({ dataflowRepository }),
  completed: GetCompleted({ dataflowRepository }),
  create: Create({ dataflowRepository }),
  dataflowDetails: GetDetails({ dataflowRepository }),
  datasetsReleasedStatus: GetReleasedDashboards({ dataflowRepository }),
  datasetsValidationStatistics: GetDatasetStatisticStatus({ dataflowRepository }),
  deleteById: Delete({ dataflowRepository }),
  newEmptyDatasetSchema: CreateDatasetSchema({ dataflowRepository }),
  pending: GetPending({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  reject: Reject({ dataflowRepository }),
  update: Update({ dataflowRepository })
};
