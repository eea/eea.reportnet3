import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';

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
import { UpdateField } from './UpdateField';
import { UpdateRecord } from './UpdateRecord';
import { ValidateData } from './ValidateData';

export const DataSetService = {
  addRecordsById: AddRecords({ dataSetRepository }),
  deleteDataById: DeleteData({ dataSetRepository }),
  deleteRecordById: DeleteRecord({ dataSetRepository }),
  deleteTableDataById: DeleteTableData({ dataSetRepository }),
  errorsById: GetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository }),
  errorStatisticsById: GetStatistics({ dataSetRepository }),
  exportDataById: ExportData({ dataSetRepository }),
  exportTableDataById: ExportTableData({ dataSetRepository }),
  schemaById: GetSchema({ dataSetRepository }),
  tableDataById: GetData({ dataSetRepository }),
  updateFieldById: UpdateField({ dataSetRepository }),
  updateRecordById: UpdateRecord({ dataSetRepository }),
  validateDataById: ValidateData({ dataSetRepository })
};
