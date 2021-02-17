import { Accept } from './Accept';
import { CloneDatasetSchemas } from './CloneDatasetSchemas';
import { Create } from './Create';
import { CreateDatasetSchema } from './CreateDatasetSchema';
import { dataflowRepository } from 'core/domain/model/Dataflow/DataflowRepository';
import { Delete } from './Delete';
import { DatasetsFinalFeedback } from './DatasetsFinalFeedback';
import { Download } from './Download';
import { GetAccepted } from './GetAccepted';
import { GetAll } from './GetAll';
import { GetAllSchemas } from './GetAllSchemas';
import { GetApiKey } from './GetApiKey';
import { GenerateApiKey } from './GenerateApiKey';
import { GetCompleted } from './GetCompleted';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetPublicDataflowInformation } from './GetPublicDataflowInformation';
import { GetDetails } from './GetDetails';
import { GetPending } from './GetPending';
import { GetPublicData } from './GetPublicData';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Reject } from './Reject';
import { Update } from './Update';
import { ValidateSchemas } from './ValidateSchemas';

export const DataflowService = {
  accept: Accept({ dataflowRepository }),
  accepted: GetAccepted({ dataflowRepository }),
  all: GetAll({ dataflowRepository }),
  cloneDatasetSchemas: CloneDatasetSchemas({ dataflowRepository }),
  completed: GetCompleted({ dataflowRepository }),
  create: Create({ dataflowRepository }),
  dataflowDetails: GetDetails({ dataflowRepository }),
  datasetsFinalFeedback: DatasetsFinalFeedback({ dataflowRepository }),
  datasetsReleasedStatus: GetReleasedDashboards({ dataflowRepository }),
  datasetsValidationStatistics: GetDatasetStatisticStatus({ dataflowRepository }),
  deleteById: Delete({ dataflowRepository }),
  downloadById: Download({ dataflowRepository }),
  getAllSchemas: GetAllSchemas({ dataflowRepository }),
  getApiKey: GetApiKey({ dataflowRepository }),
  getPublicDataflowInformation: GetPublicDataflowInformation({ dataflowRepository }),
  generateApiKey: GenerateApiKey({ dataflowRepository }),
  newEmptyDatasetSchema: CreateDatasetSchema({ dataflowRepository }),
  pending: GetPending({ dataflowRepository }),
  publicData: GetPublicData({ dataflowRepository }),
  reject: Reject({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  schemasValidation: ValidateSchemas({ dataflowRepository }),
  update: Update({ dataflowRepository })
};
