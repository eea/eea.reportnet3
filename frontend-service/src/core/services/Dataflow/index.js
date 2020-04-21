import { Accept } from './Accept';
import { Create } from './Create';
import { CreateDatasetSchema } from './CreateDatasetSchema';
import { dataflowRepository } from 'core/domain/model/Dataflow/DataflowRepository';
import { Delete } from './Delete';
import { GetAccepted } from './GetAccepted';
import { GetAll } from './GetAll';
import { GetAllSchemas } from './GetAllSchemas';
import { GetApiKey } from './GetApiKey';
import { GetCompleted } from './GetCompleted';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetDetails } from './GetDetails';
import { GetPending } from './GetPending';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Reject } from './Reject';
import { Update } from './Update';
import { ValidateSchemas } from './ValidateSchemas';

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
  getAllSchemas: GetAllSchemas({ dataflowRepository }),
  getApiKey: GetApiKey({ dataflowRepository }),
  newEmptyDatasetSchema: CreateDatasetSchema({ dataflowRepository }),
  pending: GetPending({ dataflowRepository }),
  reject: Reject({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  schemasValidation: ValidateSchemas({ dataflowRepository }),
  update: Update({ dataflowRepository })
};
