import { datasetRepository } from 'core/domain/model/Dataset/DatasetRepository';

import { AddRecords } from './AddRecords';
import { AddRecordFieldDesign } from './AddRecordFieldDesign';
import { AddTableDesign } from './AddTableDesign';
import { CreateValidation } from './CreateValidation';
import { DeleteData } from './DeleteData';
import { DeleteRecord } from './DeleteRecord';
import { DeleteRecordFieldDesign } from './DeleteRecordFieldDesign';
import { DeleteSchema } from './DeleteSchema';
import { DeleteTableData } from './DeleteTableData';
import { DeleteTableDesign } from './DeleteTableDesign';
import { ExportData } from './ExportData';
import { ExportTableData } from './ExportTableData';
import { GetData } from './GetData';
import { GetErrors } from './GetErrors';
import { GetErrorPosition } from './GetErrorPosition';
import { GetMetaData } from './GetMetaData';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { GetWebFormData } from './GetWebFormData';
import { OrderRecordFieldDesign } from './OrderRecordFieldDesign';
import { OrderTableDesign } from './OrderTableDesign';
import { UpdateField } from './UpdateField';
import { UpdateRecord } from './UpdateRecord';
import { UpdateRecordFieldDesign } from './UpdateRecordFieldDesign';
import { UpdateSchemaName } from './UpdateSchemaName';
import { UpdateTableNameDesign } from './UpdateTableNameDesign';
import { ValidateData } from './ValidateData';

export const DatasetService = {
  addRecordFieldDesign: AddRecordFieldDesign({ datasetRepository }),
  addRecordsById: AddRecords({ datasetRepository }),
  addTableDesign: AddTableDesign({ datasetRepository }),
  createValidation: CreateValidation({ datasetRepository }),
  deleteDataById: DeleteData({ datasetRepository }),
  deleteRecordById: DeleteRecord({ datasetRepository }),
  deleteRecordFieldDesign: DeleteRecordFieldDesign({ datasetRepository }),
  deleteSchemaById: DeleteSchema({ datasetRepository }),
  deleteTableDataById: DeleteTableData({ datasetRepository }),
  deleteTableDesign: DeleteTableDesign({ datasetRepository }),
  errorsById: GetErrors({ datasetRepository }),
  errorPositionByObjectId: GetErrorPosition({ datasetRepository }),
  errorStatisticsById: GetStatistics({ datasetRepository }),
  exportDataById: ExportData({ datasetRepository }),
  exportTableDataById: ExportTableData({ datasetRepository }),
  getMetaData: GetMetaData({ datasetRepository }),
  orderRecordFieldDesign: OrderRecordFieldDesign({ datasetRepository }),
  orderTableDesign: OrderTableDesign({ datasetRepository }),
  schemaById: GetSchema({ datasetRepository }),
  tableDataById: GetData({ datasetRepository }),
  updateFieldById: UpdateField({ datasetRepository }),
  updateRecordFieldDesign: UpdateRecordFieldDesign({ datasetRepository }),
  updateRecordsById: UpdateRecord({ datasetRepository }),
  updateSchemaNameById: UpdateSchemaName({ datasetRepository }),
  updateTableNameDesign: UpdateTableNameDesign({ datasetRepository }),
  validateDataById: ValidateData({ datasetRepository }),
  webFormDataById: GetWebFormData({ datasetRepository })
};
