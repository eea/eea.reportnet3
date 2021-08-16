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
import { GetRepresentativesUsersList } from './GetRepresentativesUsersList';
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
  generateApiKey: GenerateApiKey({ dataflowRepository }),
  getAllDataflowsUserList: GetAllDataflowsUserList({ dataflowRepository }),
  getRepresentativesUsersList: GetRepresentativesUsersList({ dataflowRepository }),
  getAllSchemas: GetAllSchemas({ dataflowRepository }),
  getApiKey: GetApiKey({ dataflowRepository }),
  getPublicDataflowData: GetPublicDataflowData({ dataflowRepository }),
  getPublicDataflowsByCountryCode: GetPublicDataflowsByCountryCode({ dataflowRepository }),
  getUserList: GetUserList({ dataflowRepository }),
  newEmptyDatasetSchema: CreateDatasetSchema({ dataflowRepository }),
  publicData: GetPublicData({ dataflowRepository }),
  reporting: GetReporting({ dataflowRepository }),
  schemasValidation: ValidateSchemas({ dataflowRepository }),
  update: Update({ dataflowRepository })
};
