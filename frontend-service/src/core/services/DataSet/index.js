import { datasetRepository } from 'core/domain/model/DataSet/DataSetRepository';

import { AddRecords } from './AddRecords';
import { CreateValidation } from './CreateValidation';
import { DeleteData } from './DeleteData';
import { DeleteRecord } from './DeleteRecord';
import { DeleteSchema } from './DeleteSchema';
import { DeleteTableData } from './DeleteTableData';
import { ExportData } from './ExportData';
import { ExportTableData } from './ExportTableData';
import { GetData } from './GetData';
import { GetErrors } from './GetErrors';
import { GetErrorPosition } from './GetErrorPosition';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { GetWebFormData } from './GetWebFormData';
import { UpdateField } from './UpdateField';
import { UpdateRecord } from './UpdateRecord';
import { UpdateSchemaName } from './UpdateSchemaName';
import { ValidateData } from './ValidateData';

export const DatasetService = {
  addRecordsById: AddRecords({ datasetRepository }),
  createValidation: CreateValidation({ datasetRepository }),
  deleteDataById: DeleteData({ datasetRepository }),
  deleteRecordById: DeleteRecord({ datasetRepository }),
  deleteSchemaById: DeleteSchema({ datasetRepository }),
  deleteTableDataById: DeleteTableData({ datasetRepository }),
  errorsById: GetErrors({ datasetRepository }),
  errorPositionByObjectId: GetErrorPosition({ datasetRepository }),
  errorStatisticsById: GetStatistics({ datasetRepository }),
  exportDataById: ExportData({ datasetRepository }),
  exportTableDataById: ExportTableData({ datasetRepository }),
  schemaById: GetSchema({ datasetRepository }),
  tableDataById: GetData({ datasetRepository }),
  updateFieldById: UpdateField({ datasetRepository }),
  updateRecordsById: UpdateRecord({ datasetRepository }),
  updateSchemaNameById: UpdateSchemaName({ datasetRepository }),
  validateDataById: ValidateData({ datasetRepository }),
  webFormDataById: GetWebFormData({ datasetRepository })
};
