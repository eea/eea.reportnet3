import { datasetRepository } from 'core/domain/model/Dataset/DatasetRepository';

import { AddRecordFieldDesign } from './AddRecordFieldDesign';
import { AddRecords } from './AddRecords';
import { AddTableDesign } from './AddTableDesign';
import { CreateValidation } from './CreateValidation';
import { DeleteData } from './DeleteData';
import { DeleteFileData } from './DeleteFileData';
import { DeleteRecord } from './DeleteRecord';
import { DeleteRecordFieldDesign } from './DeleteRecordFieldDesign';
import { DeleteSchema } from './DeleteSchema';
import { DeleteTableData } from './DeleteTableData';
import { DeleteTableDesign } from './DeleteTableDesign';
import { DownloadDatasetFileData } from './DownloadDatasetFileData';
import { DownloadExportDatasetFile } from './DownloadExportDatasetFile';
import { DownloadExportFile } from './DownloadExportFile';
import { DownloadFileData } from './DownloadFileData';
import { DownloadReferenceDatasetFileData } from './DownloadReferenceDatasetFileData';
import { ExportData } from './ExportData';
import { ExportDatasetDataExternal } from './ExportDatasetDataExternal';
import { ExportTableData } from './ExportTableData';
import { ExportTableSchema } from './ExportTableSchema';
import { GetData } from './GetData';
import { GetMetaData } from './GetMetaData';
import { GetReferencedFieldValues } from './GetReferencedFieldValues';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { GroupedErrors } from './GroupedErrors';
import { OrderRecordFieldDesign } from './OrderRecordFieldDesign';
import { OrderTableDesign } from './OrderTableDesign';
import { ToggleUpdatable } from './ToggleUpdatable';
import { UpdateDatasetFeedbackStatus } from './UpdateDatasetFeedbackStatus';
import { UpdateDatasetSchemaDesign } from './UpdateDatasetSchemaDesign';
import { UpdateField } from './UpdateField';
import { UpdateRecord } from './UpdateRecord';
import { UpdateRecordFieldDesign } from './UpdateRecordFieldDesign';
import { UpdateSchemaName } from './UpdateSchemaName';
import { UpdateTableDescriptionDesign } from './UpdateTableDescriptionDesign';
import { UpdateTableNameDesign } from './UpdateTableNameDesign';
import { ValidateData } from './ValidateData';
import { ValidateSqlRules } from './ValidateSqlRules';

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
  downloadDatasetFileData: DownloadDatasetFileData({ datasetRepository }),
  downloadExportDatasetFile: DownloadExportDatasetFile({ datasetRepository }),
  downloadExportFile: DownloadExportFile({ datasetRepository }),
  downloadFileData: DownloadFileData({ datasetRepository }),
  downloadReferenceDatasetFileData: DownloadReferenceDatasetFileData({ datasetRepository }),
  errorStatisticsById: GetStatistics({ datasetRepository }),
  exportDataById: ExportData({ datasetRepository }),
  exportDatasetDataExternal: ExportDatasetDataExternal({ datasetRepository }),
  exportTableDataById: ExportTableData({ datasetRepository }),
  exportTableSchemaById: ExportTableSchema({ datasetRepository }),
  getMetaData: GetMetaData({ datasetRepository }),
  getReferencedFieldValues: GetReferencedFieldValues({ datasetRepository }),
  groupedErrorsById: GroupedErrors({ datasetRepository }),
  orderRecordFieldDesign: OrderRecordFieldDesign({ datasetRepository }),
  orderTableDesign: OrderTableDesign({ datasetRepository }),
  schemaById: GetSchema({ datasetRepository }),
  tableDataById: GetData({ datasetRepository }),
  toggleUpdatable: ToggleUpdatable({ datasetRepository }),
  updateDatasetFeedbackStatus: UpdateDatasetFeedbackStatus({ datasetRepository }),
  updateDatasetSchemaDesign: UpdateDatasetSchemaDesign({ datasetRepository }),
  updateFieldById: UpdateField({ datasetRepository }),
  updateRecordFieldDesign: UpdateRecordFieldDesign({ datasetRepository }),
  updateRecordsById: UpdateRecord({ datasetRepository }),
  updateSchemaNameById: UpdateSchemaName({ datasetRepository }),
  updateTableDescriptionDesign: UpdateTableDescriptionDesign({ datasetRepository }),
  updateTableNameDesign: UpdateTableNameDesign({ datasetRepository }),
  validateDataById: ValidateData({ datasetRepository }),
  validateSqlRules: ValidateSqlRules({ datasetRepository })
};
