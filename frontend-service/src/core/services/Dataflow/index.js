import { CloneDatasetSchemas } from './CloneDatasetSchemas';
import { Create } from './Create';
import { CreateDatasetSchema } from './CreateDatasetSchema';
import { dataflowRepository } from 'core/domain/model/Dataflow/DataflowRepository';
import { DatasetsFinalFeedback } from './DatasetsFinalFeedback';
import { Delete } from './Delete';
import { Download } from './Download';
import { GenerateApiKey } from './GenerateApiKey';
import { GetAll } from './GetAll';
import { GetAllSchemas } from './GetAllSchemas';
import { GetAllDataflowsUserList } from './GetAllDataflowsUserList';
import { GetUserList } from './GetUserList';
import { GetApiKey } from './GetApiKey';
import { GetDatasetStatisticStatus } from './GetDatasetStatisticStatus';
import { GetDetails } from './GetDetails';
import { GetPublicDataflowsByCountryCode } from './GetPublicDataflowsByCountryCode';
import { GetPublicData } from './GetPublicData';
import { GetPublicDataflowData } from './GetPublicDataflowData';
import { GetReleasedDashboards } from './GetReleasedDashboards';
import { GetReporting } from './GetReporting';
import { Update } from './Update';
import { ValidateSchemas } from './ValidateSchemas';

export const DataflowService = {
  all: GetAll({ dataflowRepository }),
  cloneDatasetSchemas: CloneDatasetSchemas({ dataflowRepository }),
  create: Create({ dataflowRepository }),
  dataflowDetails: GetDetails({ dataflowRepository }),
  datasetsFinalFeedback: DatasetsFinalFeedback({ dataflowRepository }),
  datasetsReleasedStatus: GetReleasedDashboards({ dataflowRepository }),
  datasetsValidationStatistics: GetDatasetStatisticStatus({ dataflowRepository }),
  deleteById: Delete({ dataflowRepository }),
  downloadById: Download({ dataflowRepository }),
  getAllDataflowsUserList: GetAllDataflowsUserList({ dataflowRepository }),
  getUserList: GetUserList({ dataflowRepository }),
  getAllSchemas: GetAllSchemas({ dataflowRepository }),
  getApiKey: GetApiKey({ dataflowRepository }),
  getPublicDataflowsByCountryCode: GetPublicDataflowsByCountryCode({ dataflowRepository }),
  getPublicDataflowData: GetPublicDataflowData({ dataflowRepository }),
  generateApiKey: GenerateApiKey({ dataflowRepository }),
  newEmptyDatasetSchema: CreateDatasetSchema({ dataflowRepository }),
  publicData: GetPublicData({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  schemasValidation: ValidateSchemas({ dataflowRepository }),
  update: Update({ dataflowRepository })
};
