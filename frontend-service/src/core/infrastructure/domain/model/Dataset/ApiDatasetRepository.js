import { isNull, isUndefined } from 'lodash';

import { apiDataset } from 'core/infrastructure/api/domain/model/Dataset';
import { CoreUtils } from 'core/infrastructure/CoreUtils';
import { DatasetError } from 'core/domain/model/Dataset/DatasetError/DatasetError';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { DatasetTable } from 'core/domain/model/Dataset/DatasetTable/DatasetTable';
import { DatasetTableField } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableField/DatasetTableField';
import { DatasetTableRecord } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableRecord';
import { Validation } from 'core/domain/model/Validation/Validation';

const addRecordFieldDesign = async (datasetId, datasetTableRecordField) => {
  const datasetTableFieldDesign = new DatasetTableField();

  datasetTableFieldDesign.idRecord = datasetTableRecordField.recordId;
  datasetTableFieldDesign.name = datasetTableRecordField.name;
  datasetTableFieldDesign.type = datasetTableRecordField.type;
  datasetTableFieldDesign.description = datasetTableRecordField.description;

  const recordsAdded = await apiDataset.addRecordFieldDesign(datasetId, datasetTableFieldDesign);
  return recordsAdded;
};

const addRecordsById = async (datasetId, tableSchemaId, records) => {
  const datasetTableRecords = [];
  records.forEach(record => {
    let fields = record.dataRow.map(DataTableFieldDTO => {
      let newField = new DatasetTableField();
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

  const recordsAdded = await apiDataset.addRecordsById(datasetId, tableSchemaId, datasetTableRecords);
  return recordsAdded;
};

const addTableDesign = async (datasetId, tableSchemaName) => {
  const tableAdded = await apiDataset.addTableDesign(datasetId, tableSchemaName);
  return tableAdded;
};

const createValidation = (entityType, id, levelError, message) => {
  const validation = new Validation(id, levelError, entityType, new Date(Date.now()).toString(), message);
  return validation;
};

const deleteDataById = async datasetId => {
  const dataDeleted = await apiDataset.deleteDataById(datasetId);
  return dataDeleted;
};

const deleteRecordFieldDesign = async (datasetId, recordId) => {
  const recordDeleted = await apiDataset.deleteRecordFieldDesign(datasetId, recordId);
  return recordDeleted;
};

const deleteRecordById = async (datasetId, recordId) => {
  return await apiDataset.deleteRecordById(datasetId, recordId);
};

const deleteSchemaById = async datasetId => {
  return await apiDataset.deleteSchemaById(datasetId);
};

const deleteTableDataById = async (datasetId, tableId) => {
  return await apiDataset.deleteTableDataById(datasetId, tableId);
};

const deleteTableDesign = async (datasetId, tableSchemaId) => {
  const dataDeleted = await apiDataset.deleteTableDesign(datasetId, tableSchemaId);
  return dataDeleted;
};

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
  const dataset = new Dataset(
    null,
    datasetErrorsDTO.idDataset,
    datasetErrorsDTO.idDatasetSchema,
    datasetErrorsDTO.nameDataSetSchema,
    datasetErrorsDTO.totalRecords,
    datasetErrorsDTO.totalFilteredRecords
  );

  const errors = datasetErrorsDTO.errors.map(
    datasetErrorDTO =>
      datasetErrorDTO &&
      new DatasetError(
        datasetErrorDTO.typeEntity,
        datasetErrorDTO.levelError,
        datasetErrorDTO.message,
        datasetErrorDTO.idObject,
        null,
        null,
        datasetErrorDTO.idTableSchema,
        datasetErrorDTO.nameTableSchema,
        datasetErrorDTO.validationDate,
        datasetErrorDTO.idValidation
      )
  );

  dataset.errors = errors;
  return dataset;
};

const errorPositionByObjectId = async (objectId, datasetId, entityType) => {
  const datasetErrorDTO = await apiDataset.errorPositionByObjectId(objectId, datasetId, entityType);

  const datasetError = new DatasetError();
  datasetError.position = datasetErrorDTO.position;
  datasetError.recordId = datasetErrorDTO.idRecord;
  datasetError.tableSchemaId = datasetErrorDTO.idTableSchema;
  datasetError.tableSchemaName = datasetErrorDTO.nameTableSchema;

  return datasetError;
};

const errorStatisticsById = async (datasetId, tableSchemaNames) => {
  const datasetTablesDTO = await apiDataset.statisticsById(datasetId);

  //Sort by schema order
  datasetTablesDTO.tables = datasetTablesDTO.tables.sort((a, b) => {
    return tableSchemaNames.indexOf(a.nameTableSchema) - tableSchemaNames.indexOf(b.nameTableSchema);
  });

  const dataset = new Dataset();
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
    return new DatasetTable(
      datasetTableDTO.tableErrors,
      datasetTableDTO.idTableSchema,
      datasetTableDTO.nameTableSchema
    );
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
  const dataset = new Dataset();
  dataset.datasetSchemaName = datasetTableDataDTO.dataSetName;
  return dataset;
};

const getAllLevelErrorsFromRuleValidations = datasetSchemaDTO => {
  const datasetSchemaObject = [datasetSchemaDTO];
  const allLevelErrorsFromRules = [];
  findObjects(datasetSchemaObject, 'rule', allLevelErrorsFromRules);
  let levelErrorsRepeated = [];
  allLevelErrorsFromRules.forEach(rule => {
    if (!isUndefined(rule.thenCondition)) {
      levelErrorsRepeated.push(rule.thenCondition[1]);
    }
  });
  let levelErrors = [...new Set(levelErrorsRepeated)];
  levelErrors = CoreUtils.orderLevelErrors(levelErrors);
  return levelErrors;
};

const orderFieldSchema = async (datasetId, position, fieldSchemaId) => {
  const fieldOrdered = await apiDataset.orderFieldSchema(datasetId, position, fieldSchemaId);
  return fieldOrdered;
};

const orderTableSchema = async (datasetId, position, tableSchemaId) => {
  const tableOrdered = await apiDataset.orderTableSchema(datasetId, position, tableSchemaId);
  return tableOrdered;
};

const findObjects = (obj, targetProp, finalResults) => {
  const getObject = theObject => {
    if (theObject instanceof Array) {
      for (let i = 0; i < theObject.length; i++) {
        getObject(theObject[i]);
      }
    } else {
      for (let prop in theObject) {
        if (theObject.hasOwnProperty(prop)) {
          if (prop.includes(targetProp) && prop !== 'ruleId') {
            finalResults.push(theObject);
          }
          if (theObject[prop] instanceof Object || theObject[prop] instanceof Array) {
            getObject(theObject[prop]);
          }
        }
      }
    }
  };
  getObject(obj);
};

const schemaById = async datasetId => {
  const datasetSchemaDTO = await apiDataset.schemaById(datasetId);
  const dataset = new Dataset();
  dataset.datasetSchemaDescription = datasetSchemaDTO.description;
  dataset.datasetSchemaId = datasetSchemaDTO.idDataSetSchema;
  dataset.datasetSchemaName = datasetSchemaDTO.nameDatasetSchema;
  dataset.levelErrorTypes = getAllLevelErrorsFromRuleValidations(datasetSchemaDTO);
  const tables = datasetSchemaDTO.tableSchemas.map(datasetTableDTO => {
    const records = !isNull(datasetTableDTO.recordSchema)
      ? [datasetTableDTO.recordSchema].map(dataTableRecordDTO => {
          const fields = !isNull(dataTableRecordDTO.fieldSchema)
            ? dataTableRecordDTO.fieldSchema.map(DataTableFieldDTO => {
                return new DatasetTableField(
                  DataTableFieldDTO.id,
                  DataTableFieldDTO.idRecord,
                  DataTableFieldDTO.name,
                  DataTableFieldDTO.type,
                  null,
                  null,
                  null,
                  DataTableFieldDTO.description,
                  DataTableFieldDTO.idCodeList
                );
              })
            : null;
          return new DatasetTableRecord(null, dataTableRecordDTO.id, dataTableRecordDTO.idRecordSchema, fields);
        })
      : null;
    const datasetTable = new DatasetTable();
    datasetTable.tableSchemaId = datasetTableDTO.idTableSchema;
    datasetTable.tableSchemaDescription = datasetTableDTO.description;
    datasetTable.tableSchemaName = datasetTableDTO.nameTableSchema;
    datasetTable.records = records;
    datasetTable.recordSchemaId = !isNull(datasetTableDTO.recordSchema)
      ? datasetTableDTO.recordSchema.idRecordSchema
      : null;

    return datasetTable;
  });

  dataset.tables = tables;

  return dataset;
};

const tableDataById = async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError) => {
  const tableDataDTO = await apiDataset.tableDataById(datasetId, tableSchemaId, pageNum, pageSize, fields, levelError);
  const table = new DatasetTable();

  if (tableDataDTO.totalRecords > 0) {
    table.tableSchemaId = tableDataDTO.idTableSchema;
    table.totalRecords = tableDataDTO.totalRecords;
    table.totalFilteredRecords = tableDataDTO.totalFilteredRecords;

    let field, record;

    const records = tableDataDTO.records.map(dataTableRecordDTO => {
      record = new DatasetTableRecord();
      const fields = dataTableRecordDTO.fields.map(DataTableFieldDTO => {
        field = new DatasetTableField();
        field.fieldId = DataTableFieldDTO.id;
        field.fieldSchemaId = DataTableFieldDTO.idFieldSchema;
        field.recordId = dataTableRecordDTO.idRecordSchema;
        field.name = DataTableFieldDTO.name;
        field.type = DataTableFieldDTO.type;
        field.value = DataTableFieldDTO.value;

        if (!isNull(DataTableFieldDTO.fieldValidations)) {
          field.validations = DataTableFieldDTO.fieldValidations.map(fieldValidation => {
            return new Validation(
              fieldValidation.id,
              fieldValidation.validation.levelError,
              fieldValidation.validation.typeEntity,
              fieldValidation.validation.validationDate,
              fieldValidation.validation.message
            );
          });
        }
        return field;
      });

      record.datasetPartitionId = dataTableRecordDTO.datasetPartitionId;
      record.providerCode = dataTableRecordDTO.dataProviderCode;
      record.recordId = dataTableRecordDTO.id;
      record.recordSchemaId = dataTableRecordDTO.idRecordSchema;
      record.fields = fields;

      if (!isNull(dataTableRecordDTO.recordValidations)) {
        record.validations = dataTableRecordDTO.recordValidations.map(recordValidation => {
          return new Validation(
            recordValidation.id,
            recordValidation.validation.levelError,
            recordValidation.validation.typeEntity,
            recordValidation.validation.validationDate,
            recordValidation.validation.message
          );
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
  const webForm = new DatasetTable();

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

    let field, record;

    const records = webFormDataDTO.records.map(webFormRecordDTO => {
      record = new DatasetTableRecord();
      let row = {};
      webFormRecordDTO.fields.forEach(webFormFieldDTO => {
        field = new DatasetTableField();
        field.fieldId = webFormFieldDTO.id;
        field.fieldSchemaId = webFormFieldDTO.idFieldSchema;
        field.recordId = webFormRecordDTO.idRecordSchema;
        field.name = webFormFieldDTO.name;
        field.type = webFormFieldDTO.type;
        field.value = webFormFieldDTO.value;

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

      return record;
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
  const datasetTableField = new DatasetTableField();
  datasetTableField.id = fieldId;
  datasetTableField.idFieldSchema = fieldSchemaId;
  datasetTableField.type = fieldType;
  datasetTableField.value = fieldValue;

  const fieldUpdated = await apiDataset.updateFieldById(datasetId, datasetTableField);
  return fieldUpdated;
};

const updateRecordFieldDesign = async (datasetId, record) => {
  const datasetTableFieldDesign = new DatasetTableField();

  datasetTableFieldDesign.id = record.fieldSchemaId;
  // datasetTableFieldDesign.idRecord = record.recordId;
  datasetTableFieldDesign.name = record.name;
  datasetTableFieldDesign.type = record.type;
  datasetTableFieldDesign.description = record.description;

  const recordUpdated = await apiDataset.updateRecordFieldDesign(datasetId, datasetTableFieldDesign);
  return recordUpdated;
};

const updateRecordsById = async (datasetId, record) => {
  const fields = record.dataRow.map(DataTableFieldDTO => {
    let newField = new DatasetTableField();
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
  const recordAdded = await apiDataset.updateRecordsById(datasetId, [datasetTableRecord]);
  return recordAdded;
};

const updateDatasetDescriptionDesign = async (datasetId, datasetSchemaDescription) => {
  console.log({ datasetSchemaDescription });
  return await apiDataset.updateSchemaDescriptionById(datasetId, datasetSchemaDescription);
};

const updateSchemaNameById = async (datasetId, datasetSchemaName) => {
  return await apiDataset.updateSchemaNameById(datasetId, datasetSchemaName);
};

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
