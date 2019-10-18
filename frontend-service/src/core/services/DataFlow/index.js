import { dataflowRepository } from 'core/domain/model/DataFlow/DataFlowRepository';
import { GetPending } from './GetPending';
import { GetAccepted } from './GetAccepted';
import { GetCompleted } from './GetCompleted';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetDetails } from './GetDetails';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Accept } from './Accept';
import { Reject } from './Reject';
import { GetAll } from './GetAll';
import { PostDatasetSchema } from './PostDatasetSchema';

export const DataflowService = {
  all: GetAll({ dataflowRepository }),
  accepted: GetAccepted({ dataflowRepository }),
  pending: GetPending({ dataflowRepository }),
  completed: GetCompleted({ dataflowRepository }),
  dataflowDetails: GetDetails({ dataflowRepository }),
  datasetsValidationStatistics: GetDatasetStatisticStatus({ dataflowRepository }),
  datasetsReleasedStatus: GetReleasedDashboards({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  accept: Accept({ dataflowRepository }),
  reject: Reject({ dataflowRepository }),
  newEmptyDatasetSchema: PostDatasetSchema({ dataflowRepository })
};
