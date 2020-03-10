import { isNull, isUndefined } from 'lodash';

import { apiDataset } from 'core/infrastructure/api/domain/model/Dataset';
import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';

import { CoreUtils } from 'core/infrastructure/CoreUtils';
import { DatasetError } from 'core/domain/model/Dataset/DatasetError/DatasetError';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { DatasetTable } from 'core/domain/model/Dataset/DatasetTable/DatasetTable';
import { DatasetTableField } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableField/DatasetTableField';
import { DatasetTableRecord } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableRecord';
import { Validation } from 'core/domain/model/Validation/Validation';

const addRecordFieldDesign = async (datasetId, datasetTableRecordField) => {
  const datasetTableFieldDesign = new DatasetTableField({});
  datasetTableFieldDesign.idRecord = datasetTableRecordField.recordId;
  datasetTableFieldDesign.name = datasetTableRecordField.name;
  datasetTableFieldDesign.type = datasetTableRecordField.type;
  datasetTableFieldDesign.description = datasetTableRecordField.description;
  datasetTableFieldDesign.codelistItems = datasetTableRecordField.codelistItems;
  datasetTableFieldDesign.required = datasetTableRecordField.required;

  return await apiDataset.addRecordFieldDesign(datasetId, datasetTableFieldDesign);
};

const addRecordsById = async (datasetId, tableSchemaId, records) => {
  const datasetTableRecords = [];
  records.forEach(record => {
    let fields = record.dataRow.map(DataTableFieldDTO => {
      let newField = new DatasetTableField({});
      newField.id = null;
      newField.idFieldSchema = DataTableFieldDTO.fieldData.fieldSchemaId;
      newField.type = DataTableFieldDTO.fieldData.type;
      newField.value = DataTableFieldDTO.fieldData[DataTableFieldDTO.fieldData.fieldSchemaId];

      return newField;
    });
    let datasetTableRecord = new DatasetTableRecord();

    datasetTableRecord.datasetPartitionId = record.dataSetPartitionId;
    datasetTableRecord.fields = fields;
    datasetTableRecord.idRecordSchema = record.recordSchemaId;
    datasetTableRecord.id = null;

    datasetTableRecords.push(datasetTableRecord);
  });

  return await apiDataset.addRecordsById(datasetId, tableSchemaId, datasetTableRecords);
};

const addTableDesign = async (datasetId, tableSchemaName) =>
  await apiDataset.addTableDesign(datasetId, tableSchemaName);

const createValidation = (entityType, id, levelError, message) =>
  new Validation({ date: new Date(Date.now()).toString(), entityType, id, levelError, message });

const deleteDataById = async datasetId => await apiDataset.deleteDataById(datasetId);

const deleteRecordFieldDesign = async (datasetId, recordId) =>
  await apiDataset.deleteRecordFieldDesign(datasetId, recordId);

const deleteRecordById = async (datasetId, recordId) => await apiDataset.deleteRecordById(datasetId, recordId);

const deleteSchemaById = async datasetId => await apiDataset.deleteSchemaById(datasetId);

const deleteTableDataById = async (datasetId, tableId) => await apiDataset.deleteTableDataById(datasetId, tableId);

const deleteTableDesign = async (datasetId, tableSchemaId) =>
  await apiDataset.deleteTableDesign(datasetId, tableSchemaId);

const errorsById = async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  levelErrorsFilter,
  typeEntitiesFilter,
  originsFilter
) => {
  const datasetErrorsDTO = await apiDataset.errorsById(
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    levelErrorsFilter,
    typeEntitiesFilter,
    originsFilter
  );
  const dataset = new Dataset({
    datasetId: datasetErrorsDTO.idDataset,
    datasetSchemaId: datasetErrorsDTO.idDatasetSchema,
    datasetSchemaName: datasetErrorsDTO.nameDataSetSchema,
    totalErrors: datasetErrorsDTO.totalRecords,
    totalFilteredErrors: datasetErrorsDTO.totalFilteredRecords
  });

  const errors = datasetErrorsDTO.errors.map(
    datasetErrorDTO =>
      datasetErrorDTO &&
      new DatasetError({
        entityType: datasetErrorDTO.typeEntity,
        levelError: datasetErrorDTO.levelError,
        message: datasetErrorDTO.message,
        objectId: datasetErrorDTO.idObject,
        tableSchemaId: datasetErrorDTO.idTableSchema,
        tableSchemaName: datasetErrorDTO.nameTableSchema,
        validationDate: datasetErrorDTO.validationDate,
        validationId: datasetErrorDTO.idValidation
      })
  );

  dataset.errors = errors;
  return dataset;
};

const errorPositionByObjectId = async (objectId, datasetId, entityType) => {
  const datasetErrorDTO = await apiDataset.errorPositionByObjectId(objectId, datasetId, entityType);

  return new DatasetError({
    position: datasetErrorDTO.position,
    recordId: datasetErrorDTO.idRecord,
    tableSchemaId: datasetErrorDTO.idTableSchema,
    tableSchemaName: datasetErrorDTO.nameTableSchema
  });
};

const errorStatisticsById = async (datasetId, tableSchemaNames) => {
  try {
    await apiDataset.statisticsById(datasetId);
  } catch (error) {
    console.error(error);
  }
  const datasetTablesDTO = await apiDataset.statisticsById(datasetId);

  //Sort by schema order
  datasetTablesDTO.tables = datasetTablesDTO.tables.sort((a, b) => {
    return tableSchemaNames.indexOf(a.nameTableSchema) - tableSchemaNames.indexOf(b.nameTableSchema);
  });

  const dataset = new Dataset({});
  dataset.datasetSchemaName = datasetTablesDTO.nameDataSetSchema;
  dataset.datasetErrors = datasetTablesDTO.datasetErrors;
  const tableStatisticValues = [];
  let levelErrors = [];
  const allDatasetLevelErrors = [];
  const datasetTables = datasetTablesDTO.tables.map(datasetTableDTO => {
    allDatasetLevelErrors.push(CoreUtils.getDashboardLevelErrorByTable(datasetTablesDTO));
    tableStatisticValues.push([
      datasetTableDTO.totalRecords -
        (datasetTableDTO.totalRecordsWithBlockers +
          datasetTableDTO.totalRecordsWithErrors +
          datasetTableDTO.totalRecordsWithWarnings +
          datasetTableDTO.totalRecordsWithInfos),
      datasetTableDTO.totalRecordsWithInfos,
      datasetTableDTO.totalRecordsWithWarnings,
      datasetTableDTO.totalRecordsWithErrors,
      datasetTableDTO.totalRecordsWithBlockers
    ]);
    return new DatasetTable({
      hasErrors: datasetTableDTO.tableErrors,
      tableSchemaId: datasetTableDTO.idTableSchema,
      tableSchemaName: datasetTableDTO.nameTableSchema
    });
  });
  const tableBarStatisticValues = tableStatisticValuesWithErrors(tableStatisticValues);
  levelErrors = [...new Set(CoreUtils.orderLevelErrors(allDatasetLevelErrors.flat()))];
  dataset.levelErrorTypes = levelErrors;

  let transposedValues = CoreUtils.transposeMatrix(tableStatisticValues);

  dataset.tableStatisticValues = CoreUtils.transposeMatrix(tableBarStatisticValues);
  dataset.tableStatisticPercentages = CoreUtils.getPercentage(transposedValues);

  dataset.tables = datasetTables;
  return dataset;
};

const tableStatisticValuesWithErrors = tableStatisticValues => {
  let tableStatisticValuesWithSomeError = [];
  let valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues).map(error => {
    return error.map(subError => {
      return subError;
    });
  });
  valuesWithValidations.map(item => {
    if (item != null && item != undefined && !item.every(value => value === 0)) {
      tableStatisticValuesWithSomeError.push(item);
    }
  });
  return tableStatisticValuesWithSomeError;
};

const exportDataById = async (datasetId, fileType) => {
  const datasetData = await apiDataset.exportDataById(datasetId, fileType);
  return datasetData;
};

const exportTableDataById = async (datasetId, tableSchemaId, fileType) => {
  const datasetTableData = await apiDataset.exportTableDataById(datasetId, tableSchemaId, fileType);
  return datasetTableData;
};

const getMetaData = async datasetId => {
  const datasetTableDataDTO = await apiDataset.getMetaData(datasetId);
  const dataset = new Dataset({
    datasetSchemaName: datasetTableDataDTO.dataSetName
  });
  return dataset;
};

const getAllLevelErrorsFromRuleValidations = rulesDTO =>
  CoreUtils.orderLevelErrors([
    ...new Set(rulesDTO.rules.map(rule => rule.thenCondition).map(condition => condition[1]))
  ]);

const orderFieldSchema = async (datasetId, position, fieldSchemaId) => {
  const fieldOrdered = await apiDataset.orderFieldSchema(datasetId, position, fieldSchemaId);
  return fieldOrdered;
};

const orderTableSchema = async (datasetId, position, tableSchemaId) => {
  const tableOrdered = await apiDataset.orderTableSchema(datasetId, position, tableSchemaId);
  return tableOrdered;
};

const schemaById = async datasetId => {
  const datasetSchemaDTO = await apiDataset.schemaById(datasetId);
  const rulesDTO = await apiValidation.getAll(datasetSchemaDTO.idDataSetSchema);

  const dataset = new Dataset({
    datasetSchemaDescription: datasetSchemaDTO.description,
    datasetSchemaId: datasetSchemaDTO.idDataSetSchema,
    datasetSchemaName: datasetSchemaDTO.nameDatasetSchema,
    levelErrorTypes: !isUndefined(rulesDTO) && rulesDTO !== '' ? getAllLevelErrorsFromRuleValidations(rulesDTO) : []
  });

  const tables = datasetSchemaDTO.tableSchemas.map(datasetTableDTO => {
    const records = !isNull(datasetTableDTO.recordSchema)
      ? [datasetTableDTO.recordSchema].map(dataTableRecordDTO => {
          const fields = !isNull(dataTableRecordDTO.fieldSchema)
            ? dataTableRecordDTO.fieldSchema.map(DataTableFieldDTO => {
                return new DatasetTableField({
                  codelistItems: DataTableFieldDTO.codelistItems,
                  description: DataTableFieldDTO.description,
                  fieldId: DataTableFieldDTO.id,
                  name: DataTableFieldDTO.name,
                  recordId: DataTableFieldDTO.idRecord,
                  required: DataTableFieldDTO.required,
                  type: DataTableFieldDTO.type
                });
              })
            : null;
          return new DatasetTableRecord({
            datasetPartitionId: dataTableRecordDTO.id,
            fields,
            recordSchemaId: dataTableRecordDTO.idRecordSchema
          });
        })
      : null;
    return new DatasetTable({
      tableSchemaId: datasetTableDTO.idTableSchema,
      tableSchemaDescription: datasetTableDTO.description,
      tableSchemaName: datasetTableDTO.nameTableSchema,
      records: records,
      recordSchemaId: !isNull(datasetTableDTO.recordSchema) ? datasetTableDTO.recordSchema.idRecordSchema : null
    });
  });

  dataset.tables = tables;

  return dataset;
};

const tableDataById = async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError) => {
  const tableDataDTO = await apiDataset.tableDataById(datasetId, tableSchemaId, pageNum, pageSize, fields, levelError);
  const table = new DatasetTable({});

  if (tableDataDTO.totalRecords > 0) {
    table.tableSchemaId = tableDataDTO.idTableSchema;
    table.totalRecords = tableDataDTO.totalRecords;
    table.totalFilteredRecords = tableDataDTO.totalFilteredRecords;

    let field;

    const records = tableDataDTO.records.map(dataTableRecordDTO => {
      const fields = dataTableRecordDTO.fields.map(DataTableFieldDTO => {
        field = new DatasetTableField({
          fieldId: DataTableFieldDTO.id,
          fieldSchemaId: DataTableFieldDTO.idFieldSchema,
          name: DataTableFieldDTO.name,
          recordId: dataTableRecordDTO.idRecordSchema,
          type: DataTableFieldDTO.type,
          value: DataTableFieldDTO.value
        });

        if (!isNull(DataTableFieldDTO.fieldValidations)) {
          field.validations = DataTableFieldDTO.fieldValidations.map(fieldValidation => {
            return new Validation({
              date: fieldValidation.validation.validationDate,
              entityType: fieldValidation.validation.typeEntity,
              id: fieldValidation.id,
              levelError: fieldValidation.validation.levelError,
              message: fieldValidation.validation.message
            });
          });
        }
        return field;
      });
      const record = new DatasetTableRecord({
        datasetPartitionId: dataTableRecordDTO.datasetPartitionId,
        providerCode: dataTableRecordDTO.dataProviderCode,
        recordId: dataTableRecordDTO.id,
        recordSchemaId: dataTableRecordDTO.idRecordSchema,
        fields: fields
      });

      if (!isNull(dataTableRecordDTO.recordValidations)) {
        record.validations = dataTableRecordDTO.recordValidations.map(recordValidation => {
          return new Validation({
            date: recordValidation.validation.validationDate,
            entityType: recordValidation.validation.typeEntity,
            id: recordValidation.id,
            levelError: recordValidation.validation.levelError,
            message: recordValidation.validation.message
          });
        });
      }
      return record;
    });

    table.records = records;
  }
  return table;
};

const webFormDataById = async (datasetId, tableSchemaId) => {
  const webFormDataDTO = await apiDataset.webFormDataById(datasetId, tableSchemaId);
  const webForm = new DatasetTable({});

  const headerFieldSchemaId = '5d666d53460a1e0001b16717';
  const valueFieldSchemaId = '5d666d53460a1e0001b16728';
  const descriptionFieldSchemaId = '5d666d53460a1e0001b1671b';
  const letterFieldSchemaId = '5d666d53460a1e0001b16721';
  const numberFieldSchemaId = '5d666d53460a1e0001b16723';

  const formData = {};
  const columnHeaders = [];
  const rows = [];
  const letters = [];
  const rowHeaders = [];
  columnHeaders.unshift('GREENHOUSE GAS SOURCE');

  if (webFormDataDTO.totalRecords > 0) {
    webForm.tableSchemaId = webFormDataDTO.idTableSchema;
    webForm.totalRecords = webFormDataDTO.totalRecords;

    let field;

    const records = webFormDataDTO.records.map(webFormRecordDTO => {
      let row = {};
      webFormRecordDTO.fields.forEach(webFormFieldDTO => {
        field = new DatasetTableField({
          fieldId: webFormFieldDTO.id,
          fieldSchemaId: webFormFieldDTO.idFieldSchema,
          name: webFormFieldDTO.name,
          recordId: webFormRecordDTO.idRecordSchema,
          type: webFormFieldDTO.type,
          value: webFormFieldDTO.value
        });

        row.type = field.type;
        row.fieldSchemaId = field.fieldSchemaId;

        if (field.fieldSchemaId === letterFieldSchemaId) {
          row.columnPosition = field.value;
        } else if (field.fieldSchemaId === numberFieldSchemaId) {
          row.rowPosition = field.value;
        } else if (field.fieldSchemaId === valueFieldSchemaId) {
          row.fieldId = webFormFieldDTO.id;
          row.value = field.value;
        } else if (field.fieldSchemaId === headerFieldSchemaId) {
          row.columnHeader = field.value;
          if (!columnHeaders.includes(field.value)) {
            columnHeaders.push(field.value);
          }
        } else if (field.fieldSchemaId === descriptionFieldSchemaId) {
          row.description = field.value;
          if (!rowHeaders.includes(field.value)) {
            rowHeaders.push(field.value);
          }
        }

        return field;
      });
      rows.push(row);

      row.recordId = field.recordId;

      if (!letters.includes(row.columnPosition)) {
        letters.push(row.columnPosition);
      }
    });
    webForm.records = records;
    webForm.rows = rows;
  }
  letters.sort();
  let dataColumns = createDataColumns(rows, letters);

  formData.columnHeaders = columnHeaders;
  formData.dataColumns = dataColumns;
  formData.rowHeaders = rowHeaders;

  return formData;
};

const createDataColumns = (rowsData, letters) => {
  let columns = [];
  letters.forEach(function(value, i) {
    let columnLetter = rowsData.filter(row => row.columnPosition === value);
    columns.push(columnLetter);
  });
  return columns;
};

const updateFieldById = async (datasetId, fieldSchemaId, fieldId, fieldType, fieldValue) => {
  const datasetTableField = new DatasetTableField({});
  datasetTableField.id = fieldId;
  datasetTableField.idFieldSchema = fieldSchemaId;
  datasetTableField.type = fieldType;
  datasetTableField.value = fieldValue;

  const fieldUpdated = await apiDataset.updateFieldById(datasetId, datasetTableField);
  return fieldUpdated;
};

const updateRecordFieldDesign = async (datasetId, record) => {
  const datasetTableFieldDesign = new DatasetTableField({});
  datasetTableFieldDesign.id = record.fieldSchemaId;
  datasetTableFieldDesign.name = record.name;
  datasetTableFieldDesign.type = record.type;
  datasetTableFieldDesign.description = record.description;
  datasetTableFieldDesign.codelistItems = record.codelistItems;
  datasetTableFieldDesign.required = record.required;
  const recordUpdated = await apiDataset.updateRecordFieldDesign(datasetId, datasetTableFieldDesign);
  return recordUpdated;
};

const updateRecordsById = async (datasetId, record) => {
  const fields = record.dataRow.map(DataTableFieldDTO => {
    let newField = new DatasetTableField({});
    newField.id = DataTableFieldDTO.fieldData.id;
    newField.idFieldSchema = DataTableFieldDTO.fieldData.fieldSchemaId;
    newField.type = DataTableFieldDTO.fieldData.type;
    newField.value = DataTableFieldDTO.fieldData[DataTableFieldDTO.fieldData.fieldSchemaId];

    return newField;
  });
  const datasetTableRecord = new DatasetTableRecord();

  datasetTableRecord.datasetPartitionId = record.dataSetPartitionId;
  datasetTableRecord.fields = fields;
  datasetTableRecord.idRecordSchema = record.recordSchemaId;
  datasetTableRecord.id = record.recordId;
  //The service will take an array of objects(records). Actually the frontend only allows one record CRUD
  return await apiDataset.updateRecordsById(datasetId, [datasetTableRecord]);
};

const updateDatasetDescriptionDesign = async (datasetId, datasetSchemaDescription) => {
  return await apiDataset.updateSchemaDescriptionById(datasetId, datasetSchemaDescription);
};

const updateSchemaNameById = async (datasetId, datasetSchemaName) =>
  await apiDataset.updateSchemaNameById(datasetId, datasetSchemaName);

const updateTableDescriptionDesign = async (tableSchemaId, tableSchemaDescription, datasetId) => {
  const tableSchemaUpdated = await apiDataset.updateTableDescriptionDesign(
    tableSchemaId,
    tableSchemaDescription,
    datasetId
  );
  return tableSchemaUpdated;
};

const updateTableNameDesign = async (tableSchemaId, tableSchemaName, datasetId) => {
  const tableSchemaUpdated = await apiDataset.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
  return tableSchemaUpdated;
};

const validateDataById = async datasetId => {
  const dataValidation = await apiDataset.validateById(datasetId);
  return dataValidation;
};

// const getPercentage = valArr => {
//   let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
//   return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
// };

// const transposeMatrix = matrix => {
//   return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
// };

export const ApiDatasetRepository = {
  addRecordFieldDesign,
  addRecordsById,
  addTableDesign,
  createValidation,
  deleteDataById,
  deleteRecordById,
  deleteRecordFieldDesign,
  deleteSchemaById,
  deleteTableDataById,
  deleteTableDesign,
  errorsById,
  errorPositionByObjectId,
  errorStatisticsById,
  exportDataById,
  exportTableDataById,
  getMetaData,
  orderFieldSchema,
  orderTableSchema,
  schemaById,
  tableDataById,
  updateFieldById,
  updateRecordFieldDesign,
  updateRecordsById,
  updateDatasetDescriptionDesign,
  updateSchemaNameById,
  updateTableDescriptionDesign,
  updateTableNameDesign,
  validateDataById,
  webFormDataById
};
