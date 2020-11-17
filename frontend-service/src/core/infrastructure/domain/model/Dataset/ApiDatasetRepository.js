import capitalize from 'lodash/capitalize';
import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

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
  datasetTableFieldDesign.codelistItems = datasetTableRecordField.codelistItems;
  datasetTableFieldDesign.description = datasetTableRecordField.description;
  datasetTableFieldDesign.idRecord = datasetTableRecordField.recordId;
  datasetTableFieldDesign.maxSize = !isNil(datasetTableRecordField.maxSize)
    ? datasetTableRecordField.maxSize.toString()
    : null;
  datasetTableFieldDesign.name = datasetTableRecordField.name;
  datasetTableFieldDesign.pk = datasetTableRecordField.pk;
  datasetTableFieldDesign.pkHasMultipleValues = datasetTableRecordField.pkHasMultipleValues;
  datasetTableFieldDesign.pkMustBeUsed = datasetTableRecordField.pkMustBeUsed;
  datasetTableFieldDesign.readOnly = datasetTableRecordField.readOnly;
  datasetTableFieldDesign.referencedField = datasetTableRecordField.referencedField;
  datasetTableFieldDesign.required = datasetTableRecordField.required;
  datasetTableFieldDesign.type = datasetTableRecordField.type;
  datasetTableFieldDesign.validExtensions = datasetTableRecordField.validExtensions;

  return await apiDataset.addRecordFieldDesign(datasetId, datasetTableFieldDesign);
};

const addRecordsById = async (datasetId, tableSchemaId, records) => {
  const datasetTableRecords = [];
  records.forEach(record => {
    let fields = record.dataRow.map(dataTableFieldDTO => {
      let newField = new DatasetTableField({});
      newField.id = null;
      newField.idFieldSchema = dataTableFieldDTO.fieldData.fieldSchemaId;
      newField.type = dataTableFieldDTO.fieldData.type;
      newField.value = parseValue(
        dataTableFieldDTO.fieldData.type,
        dataTableFieldDTO.fieldData[dataTableFieldDTO.fieldData.fieldSchemaId],
        true
      );

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

const deleteFileData = async (datasetId, fieldId) => await apiDataset.deleteFileData(datasetId, fieldId);

const deleteRecordFieldDesign = async (datasetId, recordId) =>
  await apiDataset.deleteRecordFieldDesign(datasetId, recordId);

const deleteRecordById = async (datasetId, recordId) => await apiDataset.deleteRecordById(datasetId, recordId);

const deleteSchemaById = async datasetId => await apiDataset.deleteSchemaById(datasetId);

const deleteTableDataById = async (datasetId, tableId) => await apiDataset.deleteTableDataById(datasetId, tableId);

const deleteTableDesign = async (datasetId, tableSchemaId) =>
  await apiDataset.deleteTableDesign(datasetId, tableSchemaId);

const downloadExportFile = async (datasetId, fileName, providerId) =>
  await apiDataset.downloadExportFile(datasetId, fileName, providerId);

const downloadFileData = async (datasetId, fieldId) => await apiDataset.downloadFileData(datasetId, fieldId);

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
    totalRecords: datasetErrorsDTO.totalRecords,
    totalFilteredErrors: datasetErrorsDTO.totalFilteredRecords
  });

  const errors = datasetErrorsDTO.errors.map(
    datasetErrorDTO =>
      datasetErrorDTO &&
      new DatasetError({
        entityType: datasetErrorDTO.typeEntity,
        fieldSchemaName: datasetErrorDTO.nameFieldSchema,
        levelError: datasetErrorDTO.levelError,
        message: datasetErrorDTO.message,
        objectId: datasetErrorDTO.idObject,
        shortCode: datasetErrorDTO.shortCode,
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

  //In design datasets the statistics are not generated until validation is executed, so we have to do a sanity check for those cases
  const tableBarStatisticValues = !isEmpty(tableStatisticValues)
    ? tableStatisticValuesWithErrors(tableStatisticValues)
    : [];
  levelErrors = [...new Set(CoreUtils.orderLevelErrors(allDatasetLevelErrors.flat()))];
  dataset.levelErrorTypes = levelErrors;

  let transposedValues = !isEmpty(tableStatisticValues) ? CoreUtils.transposeMatrix(tableStatisticValues) : [];

  dataset.tableStatisticValues = !isEmpty(tableStatisticValues)
    ? CoreUtils.transposeMatrix(tableBarStatisticValues)
    : [];
  dataset.tableStatisticPercentages = !isEmpty(tableStatisticValues) ? CoreUtils.getPercentage(transposedValues) : [];

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

const exportDatasetDataExternal = async (datasetId, fileExtension) => {
  const datasetData = await apiDataset.exportDatasetDataExternal(datasetId, fileExtension);
  return datasetData;
};

const exportTableDataById = async (datasetId, tableSchemaId, fileType) => {
  const datasetTableData = await apiDataset.exportTableDataById(datasetId, tableSchemaId, fileType);
  return datasetTableData;
};

const getMetaData = async datasetId => {
  const datasetTableDataDTO = await apiDataset.getMetaData(datasetId);
  const dataset = new Dataset({
    datasetSchemaName: datasetTableDataDTO.dataSetName,
    datasetSchemaId: datasetTableDataDTO.datasetSchema,
    datasetFeedbackStatus:
      !isNil(datasetTableDataDTO.status) && capitalize(datasetTableDataDTO.status.split('_').join(' '))
  });
  return dataset;
};

const getReferencedFieldValues = async (datasetId, fieldSchemaId, searchToken) => {
  const referencedFieldValuesDTO = await apiDataset.getReferencedFieldValues(datasetId, fieldSchemaId, searchToken);
  return referencedFieldValuesDTO.map(
    referencedFieldDTO =>
      new DatasetTableField({
        fieldId: referencedFieldDTO.id,
        fieldSchemaId: referencedFieldDTO.idFieldSchema,
        type: referencedFieldDTO.type,
        value: referencedFieldDTO.value
      })
  );
};

const getAllLevelErrorsFromRuleValidations = rulesDTO =>
  CoreUtils.orderLevelErrors([
    ...new Set(rulesDTO.rules.map(rule => rule.thenCondition).map(condition => condition[1]))
  ]);

const groupedErrorsById = async (
  datasetId,
  pageNum,
  pageSize,
  sortField,
  asc,
  levelErrorsFilter,
  typeEntitiesFilter,
  originsFilter
) => {
  const datasetErrorsDTO = await apiDataset.groupedErrorsById(
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
    totalErrors: datasetErrorsDTO.totalErrors,
    totalRecords: datasetErrorsDTO.totalRecords,
    totalFilteredErrors: datasetErrorsDTO.totalFilteredRecords
  });

  const errors = datasetErrorsDTO.errors.map(
    datasetErrorDTO =>
      datasetErrorDTO &&
      new DatasetError({
        entityType: datasetErrorDTO.typeEntity,
        fieldSchemaName: datasetErrorDTO.nameFieldSchema,
        levelError: datasetErrorDTO.levelError,
        message: datasetErrorDTO.message,
        numberOfRecords: datasetErrorDTO.numberOfRecords,
        objectId: datasetErrorDTO.idObject,
        ruleId: datasetErrorDTO.idRule,
        shortCode: datasetErrorDTO.shortCode,
        tableSchemaId: datasetErrorDTO.idTableSchema,
        tableSchemaName: datasetErrorDTO.nameTableSchema,
        validationDate: datasetErrorDTO.validationDate,
        validationId: datasetErrorDTO.idValidation
      })
  );

  dataset.errors = errors;
  return dataset;
};

const isValidJSON = value => {
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) return false;

  try {
    JSON.parse(value);
  } catch (e) {
    return false;
  }
  return true;
};

const orderFieldSchema = async (datasetId, position, fieldSchemaId) => {
  const fieldOrdered = await apiDataset.orderFieldSchema(datasetId, position, fieldSchemaId);
  return fieldOrdered;
};

const orderTableSchema = async (datasetId, position, tableSchemaId) => {
  const tableOrdered = await apiDataset.orderTableSchema(datasetId, position, tableSchemaId);
  return tableOrdered;
};

const parseValue = (type, value, feToBe = false) => {
  if (['POINT', 'LINESTRING', 'POLYGON'].includes(type) && value !== '' && !isNil(value)) {
    if (!isValidJSON(value)) {
      return '';
    }
    const inmValue = JSON.parse(cloneDeep(value));
    inmValue.geometry.coordinates = [inmValue.geometry.coordinates[1], inmValue.geometry.coordinates[0]];
    if (!feToBe) {
      inmValue.properties.srid = `EPSG:${inmValue.properties.srid}`;
    } else {
      inmValue.properties.srid = inmValue.properties.srid.split(':')[1];
    }
    return JSON.stringify(inmValue);
  }
  return value;
};

const schemaById = async datasetId => {
  const datasetSchemaDTO = await apiDataset.schemaById(datasetId);
  const rulesDTO = await apiValidation.getAll(datasetSchemaDTO.idDataSetSchema);

  const dataset = new Dataset({
    datasetSchemaDescription: datasetSchemaDTO.description,
    datasetSchemaId: datasetSchemaDTO.idDataSetSchema,
    datasetSchemaName: datasetSchemaDTO.nameDatasetSchema,
    levelErrorTypes: !isUndefined(rulesDTO) && rulesDTO !== '' ? getAllLevelErrorsFromRuleValidations(rulesDTO) : [],
    webform: datasetSchemaDTO.webform ? datasetSchemaDTO.webform.name : null
  });

  const tables = datasetSchemaDTO.tableSchemas.map(datasetTableDTO => {
    const records = !isNull(datasetTableDTO.recordSchema)
      ? [datasetTableDTO.recordSchema].map(dataTableRecordDTO => {
          const fields = !isNull(dataTableRecordDTO.fieldSchema)
            ? dataTableRecordDTO.fieldSchema.map(dataTableFieldDTO => {
                return new DatasetTableField({
                  codelistItems: dataTableFieldDTO.codelistItems,
                  description: dataTableFieldDTO.description,
                  fieldId: dataTableFieldDTO.id,
                  maxSize: dataTableFieldDTO.maxSize,
                  pk: !isNull(dataTableFieldDTO.pk) ? dataTableFieldDTO.pk : false,
                  pkHasMultipleValues: !isNull(dataTableFieldDTO.pkHasMultipleValues)
                    ? dataTableFieldDTO.pkHasMultipleValues
                    : false,
                  pkMustBeUsed: !isNull(dataTableFieldDTO.pkMustBeUsed) ? dataTableFieldDTO.pkMustBeUsed : false,
                  pkReferenced: !isNull(dataTableFieldDTO.pkReferenced) ? dataTableFieldDTO.pkReferenced : false,
                  name: dataTableFieldDTO.name,
                  readOnly: dataTableFieldDTO.readOnly,
                  recordId: dataTableFieldDTO.idRecord,
                  referencedField: dataTableFieldDTO.referencedField,
                  required: dataTableFieldDTO.required,
                  type: dataTableFieldDTO.type,
                  validExtensions: !isNull(dataTableFieldDTO.validExtensions) ? dataTableFieldDTO.validExtensions : []
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
      hasPKReferenced: !isEmpty(
        records.filter(record => record.fields.filter(field => field.pkReferenced === true)[0])
      ),
      tableSchemaToPrefill: isNull(datasetTableDTO.toPrefill) ? false : datasetTableDTO.toPrefill,
      tableSchemaId: datasetTableDTO.idTableSchema,
      tableSchemaDescription: datasetTableDTO.description,
      tableSchemaFixedNumber: isNull(datasetTableDTO.fixedNumber) ? false : datasetTableDTO.fixedNumber,
      tableSchemaName: datasetTableDTO.nameTableSchema,
      tableSchemaNotEmpty: isNull(datasetTableDTO.notEmpty) ? false : datasetTableDTO.notEmpty,
      tableSchemaReadOnly: isNull(datasetTableDTO.readOnly) ? false : datasetTableDTO.readOnly,
      records: records,
      recordSchemaId: !isNull(datasetTableDTO.recordSchema) ? datasetTableDTO.recordSchema.idRecordSchema : null
    });
  });
  dataset.tables = tables;

  return dataset;
};

const tableDataById = async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError, ruleId) => {
  const tableDataDTO = await apiDataset.tableDataById(
    datasetId,
    tableSchemaId,
    pageNum,
    pageSize,
    fields,
    levelError,
    ruleId
  );
  const table = new DatasetTable({});

  table.tableSchemaId = tableDataDTO.idTableSchema;
  table.totalRecords = ruleId === '' ? tableDataDTO.totalRecords : tableDataDTO.totalFilteredRecords;
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
        value: parseValue(DataTableFieldDTO.type, DataTableFieldDTO.value)
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

  return table;
};

const updateFieldById = async (datasetId, fieldSchemaId, fieldId, fieldType, fieldValue) => {
  const datasetTableField = new DatasetTableField({});
  datasetTableField.id = fieldId;
  datasetTableField.idFieldSchema = fieldSchemaId;
  datasetTableField.type = fieldType;
  datasetTableField.value = parseValue(fieldType, fieldValue, true);

  return await apiDataset.updateFieldById(datasetId, datasetTableField);
};

const updateRecordFieldDesign = async (datasetId, record) => {
  const datasetTableFieldDesign = new DatasetTableField({});
  datasetTableFieldDesign.codelistItems = record.codelistItems;
  datasetTableFieldDesign.description = record.description;
  datasetTableFieldDesign.id = record.fieldSchemaId;
  datasetTableFieldDesign.idRecord = record.recordId;
  datasetTableFieldDesign.maxSize = !isNil(record.maxSize) ? record.maxSize.toString() : null;
  datasetTableFieldDesign.name = record.name;
  datasetTableFieldDesign.pk = record.pk;
  datasetTableFieldDesign.pkHasMultipleValues = record.pkHasMultipleValues;
  datasetTableFieldDesign.pkMustBeUsed = record.pkMustBeUsed;
  datasetTableFieldDesign.readOnly = record.readOnly;
  datasetTableFieldDesign.referencedField = record.referencedField;
  datasetTableFieldDesign.required = record.required;
  datasetTableFieldDesign.type = record.type;
  datasetTableFieldDesign.validExtensions = record.validExtensions;
  const recordUpdated = await apiDataset.updateRecordFieldDesign(datasetId, datasetTableFieldDesign);
  return recordUpdated;
};

const updateRecordsById = async (datasetId, record) => {
  const fields = record.dataRow.map(dataTableFieldDTO => {
    let newField = new DatasetTableField({});
    newField.id = dataTableFieldDTO.fieldData.id;
    newField.idFieldSchema = dataTableFieldDTO.fieldData.fieldSchemaId;
    newField.type = dataTableFieldDTO.fieldData.type;
    newField.value = parseValue(
      dataTableFieldDTO.fieldData.type,
      dataTableFieldDTO.fieldData[dataTableFieldDTO.fieldData.fieldSchemaId],
      true
    );

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

const updateDatasetFeedbackStatus = async (dataflowId, datasetId, message, feedbackStatus) => {
  return await apiDataset.updateDatasetFeedbackStatus(
    dataflowId,
    datasetId,
    message,
    feedbackStatus.toUpperCase().split(' ').join('_')
  );
};

const updateDatasetSchemaDesign = async (datasetId, datasetSchema) => {
  return await apiDataset.updateDatasetSchemaById(datasetId, datasetSchema);
};

const updateSchemaNameById = async (datasetId, datasetSchemaName) =>
  await apiDataset.updateSchemaNameById(datasetId, datasetSchemaName);

const updateTableDescriptionDesign = async (
  tableSchemaToPrefill,
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId,
  tableSchemaNotEmpty,
  tableSchemaFixedNumber
) => {
  const tableSchemaUpdated = await apiDataset.updateTableDescriptionDesign(
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
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

const validateSqlRules = async (datasetId, datasetSchemaId) => {
  return await apiDataset.validateSqlRules(datasetId, datasetSchemaId);
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
  deleteFileData,
  deleteRecordById,
  deleteRecordFieldDesign,
  deleteSchemaById,
  deleteTableDataById,
  deleteTableDesign,
  downloadExportFile,
  downloadFileData,
  errorPositionByObjectId,
  errorsById,
  errorStatisticsById,
  exportDataById,
  exportDatasetDataExternal,
  exportTableDataById,
  getMetaData,
  getReferencedFieldValues,
  groupedErrorsById,
  orderFieldSchema,
  orderTableSchema,
  schemaById,
  tableDataById,
  updateDatasetSchemaDesign,
  updateDatasetFeedbackStatus,
  updateFieldById,
  updateRecordFieldDesign,
  updateRecordsById,
  updateSchemaNameById,
  updateTableDescriptionDesign,
  updateTableNameDesign,
  validateDataById,
  validateSqlRules
};
