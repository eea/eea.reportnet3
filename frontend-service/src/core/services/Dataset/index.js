import { datasetRepository } from 'core/domain/model/Dataset/DatasetRepository';

import { AddRecords } from './AddRecords';
import { AddRecordFieldDesign } from './AddRecordFieldDesign';
import { AddTableDesign } from './AddTableDesign';
import { CreateValidation } from './CreateValidation';
import { DeleteData } from './DeleteData';
import { DeleteFileData } from './DeleteFileData';
import { DeleteRecord } from './DeleteRecord';
import { DeleteRecordFieldDesign } from './DeleteRecordFieldDesign';
import { DeleteSchema } from './DeleteSchema';
import { DeleteTableData } from './DeleteTableData';
import { DeleteTableDesign } from './DeleteTableDesign';
import { DownloadExportFile } from './DownloadExportFile';
import { DownloadFileData } from './DownloadFileData';
import { ExportData } from './ExportData';
import { ExportDatasetDataExternal } from './ExportDatasetDataExternal';
import { ExportTableData } from './ExportTableData';
import { GetData } from './GetData';
import { GetErrorPosition } from './GetErrorPosition';
import { GetErrors } from './GetErrors';
import { GetMetaData } from './GetMetaData';
import { GetReferencedFieldValues } from './GetReferencedFieldValues';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { GroupedErrors } from './GroupedErrors';
import { OrderRecordFieldDesign } from './OrderRecordFieldDesign';
import { OrderTableDesign } from './OrderTableDesign';
import { UpdateDatasetDescriptionDesign } from './UpdateDatasetDescriptionDesign';
import { UpdateField } from './UpdateField';
import { UpdateRecord } from './UpdateRecord';
import { UpdateRecordFieldDesign } from './UpdateRecordFieldDesign';
import { UpdateSchemaName } from './UpdateSchemaName';
import { UpdateTableDescriptionDesign } from './UpdateTableDescriptionDesign';
import { UpdateTableNameDesign } from './UpdateTableNameDesign';
import { ValidateData } from './ValidateData';

export const DatasetService = {
  addRecordFieldDesign: AddRecordFieldDesign({ datasetRepository }),
  addRecordsById: AddRecords({ datasetRepository }),
  addTableDesign: AddTableDesign({ datasetRepository }),
  createValidation: CreateValidation({ datasetRepository }),
  deleteDataById: DeleteData({ datasetRepository }),
  deleteFileData: DeleteFileData({ datasetRepository }),
  deleteRecordById: DeleteRecord({ datasetRepository }),
  deleteRecordFieldDesign: DeleteRecordFieldDesign({ datasetRepository }),
  deleteSchemaById: DeleteSchema({ datasetRepository }),
  deleteTableDataById: DeleteTableData({ datasetRepository }),
  deleteTableDesign: DeleteTableDesign({ datasetRepository }),
  downloadExportFile: DownloadExportFile({ datasetRepository }),
  downloadFileData: DownloadFileData({ datasetRepository }),
  errorsById: GetErrors({ datasetRepository }),
  errorPositionByObjectId: GetErrorPosition({ datasetRepository }),
  errorStatisticsById: GetStatistics({ datasetRepository }),
  exportDataById: ExportData({ datasetRepository }),
  exportDatasetDataExternal: ExportDatasetDataExternal({ datasetRepository }),
  exportTableDataById: ExportTableData({ datasetRepository }),
  getMetaData: GetMetaData({ datasetRepository }),
  getReferencedFieldValues: GetReferencedFieldValues({ datasetRepository }),
  groupedErrorsById: GroupedErrors({ datasetRepository }),
  orderRecordFieldDesign: OrderRecordFieldDesign({ datasetRepository }),
  orderTableDesign: OrderTableDesign({ datasetRepository }),
  schemaById: GetSchema({ datasetRepository }),
  tableDataById: GetData({ datasetRepository }),
  updateDatasetDescriptionDesign: UpdateDatasetDescriptionDesign({ datasetRepository }),
  updateFieldById: UpdateField({ datasetRepository }),
  updateRecordFieldDesign: UpdateRecordFieldDesign({ datasetRepository }),
  updateRecordsById: UpdateRecord({ datasetRepository }),
  updateSchemaNameById: UpdateSchemaName({ datasetRepository }),
  updateTableDescriptionDesign: UpdateTableDescriptionDesign({ datasetRepository }),
  updateTableNameDesign: UpdateTableNameDesign({ datasetRepository }),
  validateDataById: ValidateData({ datasetRepository })
};
