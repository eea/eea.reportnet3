import { datasetRepository } from 'core/domain/model/DataSet/DataSetRepository';

import { AddRecords } from './AddRecords';
import { DeleteData } from './DeleteData';
import { DeleteRecord } from './DeleteRecord';
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
import { ValidateData } from './ValidateData';

export const DatasetService = {
  addRecordsById: AddRecords({ datasetRepository }),
  deleteDataById: DeleteData({ datasetRepository }),
  deleteRecordById: DeleteRecord({ datasetRepository }),
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
  validateDataById: ValidateData({ datasetRepository }),
  webFormDataById: GetWebFormData({ datasetRepository })
};
