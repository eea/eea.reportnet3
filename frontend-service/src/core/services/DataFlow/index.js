import { dataflowRepository } from 'core/domain/model/DataFlow/DataFlowRepository';
import { GetPending } from './GetPending';
import { GetAccepted } from './GetAccepted';
import { GetCompleted } from './GetCompleted';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Accept } from './Accept';
import { Reject } from './Reject';
import { GetAll } from './GetAll';

export const DataflowService = {
  all: GetAll({ dataflowRepository }),
  accepted: GetAccepted({ dataflowRepository }),
  pending: GetPending({ dataflowRepository }),
  completed: GetCompleted({ dataflowRepository }),
  datasetValidationStatistics: GetDatasetStatisticStatus({ dataflowRepository }),
  datasetReleasedStatus: GetReleasedDashboards({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  accept: Accept({ dataflowRepository }),
  reject: Reject({ dataflowRepository })
};
