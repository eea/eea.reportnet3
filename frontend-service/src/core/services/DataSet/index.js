import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';

import { DeleteData } from './DeleteData';
import { DeleteTableData } from './DeleteTableData';
import { ExportData } from './ExportData';
import { ExportTableData } from './ExportTableData';
import { GetData } from './GetData';
import { GetErrors } from './GetErrors';
import { GetErrorPosition } from './GetErrorPosition';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { ValidateData } from './ValidateData';

export const DataSetService = {
  deleteDataById: DeleteData({ dataSetRepository }),
  deleteTableDataById: DeleteTableData({ dataSetRepository }),
  errorsById: GetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository }),
  errorStatisticsById: GetStatistics({ dataSetRepository }),
  exportDataById: ExportData({ dataSetRepository }),
  exportTableDataById: ExportTableData({ dataSetRepository }),
  schemaById: GetSchema({ dataSetRepository }),
  tableDataById: GetData({ dataSetRepository }),
  validateDataById: ValidateData({ dataSetRepository })
};
