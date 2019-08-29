import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';

import { AddRecord } from './AddRecord';
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

export const DataSetService = {
  addRecordById: AddRecord({ dataSetRepository }),
  deleteDataById: DeleteData({ dataSetRepository }),
  deleteRecordByIds: DeleteRecord({ dataSetRepository }),
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
  validateDataById: ValidateData({ dataSetRepository }),
  webFormDataById: GetWebFormData({ dataSetRepository })
};
