import capitalize from 'lodash/capitalize';
import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { DatasetRepository } from 'repositories/DatasetRepository';
import { ValidationRepository } from 'repositories/ValidationRepository';

import { Dataset } from 'entities/Dataset';
import { DatasetError } from 'entities/DatasetError';
import { DatasetTable } from 'entities/DatasetTable';
import { DatasetTableField } from 'entities/DatasetTableField';
import { DatasetTableRecord } from 'entities/DatasetTableRecord';
import { Validation } from 'entities/Validation';

import { CoreUtils } from 'repositories/_utils/CoreUtils';

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

  return await DatasetRepository.addRecordFieldDesign(datasetId, datasetTableFieldDesign);
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

  return await DatasetRepository.addRecordsById(datasetId, tableSchemaId, datasetTableRecords);
};

const addTableDesign = async (datasetId, tableSchemaName) =>
  await DatasetRepository.addTableDesign(datasetId, tableSchemaName);

const createValidation = (entityType, id, levelError, message) =>
  new Validation({ date: new Date(Date.now()).toString(), entityType, id, levelError, message });

const deleteDataById = async datasetId => await DatasetRepository.deleteDataById(datasetId);

const deleteFileData = async (datasetId, fieldId) => await DatasetRepository.deleteFileData(datasetId, fieldId);

const deleteRecordFieldDesign = async (datasetId, recordId) =>
  await DatasetRepository.deleteRecordFieldDesign(datasetId, recordId);

const deleteRecordById = async (datasetId, recordId, deleteInCascade) =>
  await DatasetRepository.deleteRecordById(datasetId, recordId, deleteInCascade);

const deleteSchemaById = async datasetId => await DatasetRepository.deleteSchemaById(datasetId);

const deleteTableDataById = async (datasetId, tableId) =>
  await DatasetRepository.deleteTableDataById(datasetId, tableId);

const deleteTableDesign = async (datasetId, tableSchemaId) =>
  await DatasetRepository.deleteTableDesign(datasetId, tableSchemaId);

const downloadExportDatasetFile = async (datasetId, fileName) =>
  await DatasetRepository.downloadExportDatasetFile(datasetId, fileName);

const downloadExportFile = async (datasetId, fileName, providerId) =>
  await DatasetRepository.downloadExportFile(datasetId, fileName, providerId);

const downloadFileData = async (dataflowId, datasetId, fieldId, dataProviderId) =>
  await DatasetRepository.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);

const downloadDatasetFileData = async (dataflowId, dataProviderId, fileName) =>
  await DatasetRepository.downloadDatasetFileData(dataflowId, dataProviderId, fileName);

const downloadReferenceDatasetFileData = async (dataflowId, fileName) =>
  await DatasetRepository.downloadReferenceDatasetFileData(dataflowId, fileName);

const errorStatisticsById = async (datasetId, tableSchemaNames) => {
  try {
    await DatasetRepository.statisticsById(datasetId);
  } catch (error) {
    console.error('ApiDatasetRepository - errorStatisticsById.', error);
  }
  const datasetTablesDTO = await DatasetRepository.statisticsById(datasetId);

  //Sort by schema order
  datasetTablesDTO.data.tables = datasetTablesDTO.data.tables.sort((a, b) => {
    return tableSchemaNames.indexOf(a.nameTableSchema) - tableSchemaNames.indexOf(b.nameTableSchema);
  });

  const dataset = new Dataset({});
  dataset.datasetSchemaName = datasetTablesDTO.data.nameDataSetSchema;
  dataset.datasetErrors = datasetTablesDTO.data.datasetErrors;
  const tableStatisticValues = [];
  let levelErrors = [];
  const allDatasetLevelErrors = [];
  const datasetTables = datasetTablesDTO.data.tables.map(datasetTableDTO => {
    allDatasetLevelErrors.push(CoreUtils.getDashboardLevelErrorByTable(datasetTablesDTO.data));
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
  datasetTablesDTO.data = dataset;
  return datasetTablesDTO;
};

const tableStatisticValuesWithErrors = tableStatisticValues => {
  let tableStatisticValuesWithSomeError = [];
  let valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues).map(error => {
    return error.map(subError => {
      return subError;
    });
  });
  valuesWithValidations.forEach(item => {
    if (!isNil(item) && !item.every(value => value === 0)) {
      tableStatisticValuesWithSomeError.push(item);
    }
  });
  return tableStatisticValuesWithSomeError;
};

const exportDataById = async (datasetId, fileType) => await DatasetRepository.exportDataById(datasetId, fileType);

const exportDatasetDataExternal = async (datasetId, integrationId) => {
  return await DatasetRepository.exportDatasetDataExternal(datasetId, integrationId);
};

const exportTableDataById = async (datasetId, tableSchemaId, fileType) => {
  return await DatasetRepository.exportTableDataById(datasetId, tableSchemaId, fileType);
};

const exportTableSchemaById = async (datasetId, datasetSchemaId, tableSchemaId, fileType) => {
  return await DatasetRepository.exportTableSchemaById(datasetId, datasetSchemaId, tableSchemaId, fileType);
};

const getMetaData = async datasetId => {
  const datasetTableDataDTO = await DatasetRepository.getMetaData(datasetId);
  datasetTableDataDTO.data = new Dataset({
    datasetFeedbackStatus:
      !isNil(datasetTableDataDTO.data.status) && capitalize(datasetTableDataDTO.data.status.split('_').join(' ')),
    datasetSchemaId: datasetTableDataDTO.data.datasetSchema,
    datasetSchemaName: datasetTableDataDTO.data.dataSetName,
    dataProviderId: datasetTableDataDTO.data.dataProviderId
  });

  return datasetTableDataDTO;
};

const getReferencedFieldValues = async (
  datasetId,
  fieldSchemaId,
  searchToken,
  conditionalValue,
  datasetSchemaId,
  resultsNumber
) => {
  const referencedFieldValuesDTO = await DatasetRepository.getReferencedFieldValues(
    datasetId,
    fieldSchemaId,
    searchToken,
    conditionalValue,
    datasetSchemaId,
    resultsNumber
  );
  referencedFieldValuesDTO.data = referencedFieldValuesDTO.data.map(
    referencedFieldDTO =>
      new DatasetTableField({
        fieldId: referencedFieldDTO.id,
        fieldSchemaId: referencedFieldDTO.idFieldSchema,
        label: referencedFieldDTO.label,
        type: referencedFieldDTO.type,
        value: referencedFieldDTO.value
      })
  );

  return referencedFieldValuesDTO;
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
  fieldValueFilter,
  levelErrorsFilter,
  typeEntitiesFilter,
  tablesFilter
) => {
  const datasetErrorsDTO = await DatasetRepository.groupedErrorsById(
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    fieldValueFilter,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  );
  const dataset = new Dataset({
    datasetId: datasetErrorsDTO.data.idDataset,
    datasetSchemaId: datasetErrorsDTO.data.idDatasetSchema,
    datasetSchemaName: datasetErrorsDTO.data.nameDataSetSchema,
    totalErrors: datasetErrorsDTO.data.totalErrors,
    totalRecords: datasetErrorsDTO.data.totalRecords,
    totalFilteredErrors: datasetErrorsDTO.data.totalFilteredRecords
  });

  const errors = datasetErrorsDTO.data.errors.map(
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
  datasetErrorsDTO.data = dataset;
  return datasetErrorsDTO;
};

const isValidJSON = value => {
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) return false;
  try {
    JSON.parse(value);
  } catch (error) {
    return false;
  }
  return true;
};

const orderFieldSchema = async (datasetId, position, fieldSchemaId) => {
  return await DatasetRepository.orderFieldSchema(datasetId, position, fieldSchemaId);
};

const orderTableSchema = async (datasetId, position, tableSchemaId) => {
  return await DatasetRepository.orderTableSchema(datasetId, position, tableSchemaId);
};

const parseValue = (type, value, feToBe = false) => {
  if (
    ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(type) &&
    value !== '' &&
    !isNil(value)
  ) {
    if (!isValidJSON(value)) {
      return '';
    }
    const inmValue = JSON.parse(cloneDeep(value));
    const parsedValue = JSON.parse(value);

    if (parsedValue.geometry.type.toUpperCase() !== type) {
      if (type.toUpperCase() === 'POINT') {
        return '';
      }
      inmValue.geometry.type = type;
      inmValue.geometry.coordinates = [];
    } else {
      switch (type.toUpperCase()) {
        case 'POINT':
          inmValue.geometry.coordinates = [parsedValue.geometry.coordinates[1], parsedValue.geometry.coordinates[0]];
          break;
        case 'MULTIPOINT':
        case 'LINESTRING':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(coordinate =>
            !isNil(coordinate) ? [coordinate[1], coordinate[0]] : []
          );
          break;
        case 'POLYGON':
        case 'MULTILINESTRING':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(coordinate => {
            if (Array.isArray(coordinate)) {
              return coordinate.map(innerCoordinate =>
                !isNil(innerCoordinate) ? [innerCoordinate[1], innerCoordinate[0]] : []
              );
            } else {
              return [];
            }
          });
          break;
        case 'MULTIPOLYGON':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(polygon => {
            if (Array.isArray(polygon)) {
              return polygon.map(coordinate => {
                if (Array.isArray(coordinate)) {
                  return coordinate.map(innerCoordinate =>
                    !isNil(innerCoordinate) ? [innerCoordinate[1], innerCoordinate[0]] : []
                  );
                } else {
                  return [];
                }
              });
            } else {
              return [];
            }
          });
          break;
        default:
          break;
      }
    }

    if (!feToBe) {
      inmValue.properties.srid = `EPSG:${parsedValue.properties.srid}`;
    } else {
      inmValue.properties.srid = parsedValue.properties.srid.split(':')[1];
    }

    return JSON.stringify(inmValue);
  }
  return value;
};

const schemaById = async datasetId => {
  const datasetSchemaDTO = await DatasetRepository.schemaById(datasetId);
  const rulesDTO = await ValidationRepository.getAll(datasetSchemaDTO.data.idDataSetSchema);

  const dataset = new Dataset({
    availableInPublic: datasetSchemaDTO.data.availableInPublic,
    datasetSchemaDescription: datasetSchemaDTO.data.description,
    datasetSchemaId: datasetSchemaDTO.data.idDataSetSchema,
    datasetSchemaName: datasetSchemaDTO.data.nameDatasetSchema,
    levelErrorTypes:
      !isUndefined(rulesDTO.data) && rulesDTO.data !== '' ? getAllLevelErrorsFromRuleValidations(rulesDTO.data) : [],
    referenceDataset: datasetSchemaDTO.data.referenceDataset,
    webform: datasetSchemaDTO.data.webform ? datasetSchemaDTO.data.webform.name : null
  });

  const tables = datasetSchemaDTO.data.tableSchemas.map(datasetTableDTO => {
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
  datasetSchemaDTO.data = dataset;

  return datasetSchemaDTO;
};

const tableDataById = async ({
  datasetId,
  fields = undefined,
  fieldSchemaId = undefined,
  levelError = null,
  pageNum,
  pageSize,
  ruleId = undefined,
  tableSchemaId,
  value = ''
}) => {
  const tableDataDTO = await DatasetRepository.tableDataById(
    datasetId,
    tableSchemaId,
    pageNum,
    pageSize,
    fields,
    levelError,
    ruleId,
    fieldSchemaId,
    value
  );
  const table = new DatasetTable({});

  table.tableSchemaId = tableDataDTO.data.idTableSchema;
  table.totalRecords = tableDataDTO.data.totalRecords;
  table.totalFilteredRecords = tableDataDTO.data.totalFilteredRecords;

  let field;

  const records = tableDataDTO.data.records.map(dataTableRecordDTO => {
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
      fields: fields,
      providerCode: dataTableRecordDTO.dataProviderCode,
      recordId: dataTableRecordDTO.id,
      recordSchemaId: dataTableRecordDTO.idRecordSchema
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
  tableDataDTO.data = table;

  return tableDataDTO;
};

const updateFieldById = async (datasetId, fieldSchemaId, fieldId, fieldType, fieldValue, updateInCascade) => {
  const datasetTableField = new DatasetTableField({});
  datasetTableField.id = fieldId;
  datasetTableField.idFieldSchema = fieldSchemaId;
  datasetTableField.type = fieldType;
  datasetTableField.value = parseValue(fieldType, fieldValue, true);

  return await DatasetRepository.updateFieldById(datasetId, datasetTableField, updateInCascade);
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

  return await DatasetRepository.updateRecordFieldDesign(datasetId, datasetTableFieldDesign);
};

const updateRecordsById = async (datasetId, record, updateInCascade) => {
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
  return await DatasetRepository.updateRecordsById(datasetId, [datasetTableRecord], updateInCascade);
};

const updateReferenceDatasetStatus = async (datasetId, updatable) =>
  await DatasetRepository.updateReferenceDatasetStatus(datasetId, updatable);

const updateDatasetFeedbackStatus = async (dataflowId, datasetId, message, feedbackStatus) => {
  return await DatasetRepository.updateDatasetFeedbackStatus(
    dataflowId,
    datasetId,
    message,
    feedbackStatus.toUpperCase().split(' ').join('_')
  );
};

const updateDatasetSchemaDesign = async (datasetId, datasetSchema) => {
  return await DatasetRepository.updateDatasetSchemaById(datasetId, datasetSchema);
};

const updateSchemaNameById = async (datasetId, datasetSchemaName) =>
  await DatasetRepository.updateSchemaNameById(datasetId, datasetSchemaName);

const updateTableDescriptionDesign = async (
  tableSchemaToPrefill,
  tableSchemaId,
  tableSchemaDescription,
  tableSchemaIsReadOnly,
  datasetId,
  tableSchemaNotEmpty,
  tableSchemaFixedNumber
) => {
  return await DatasetRepository.updateTableDescriptionDesign(
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
  );
};

const updateTableNameDesign = async (tableSchemaId, tableSchemaName, datasetId) => {
  return await DatasetRepository.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
};

const validateDataById = async datasetId => await DatasetRepository.validateById(datasetId);

const validateSqlRules = async (datasetId, datasetSchemaId) => {
  return await DatasetRepository.validateSqlRules(datasetId, datasetSchemaId);
};

// const getPercentage = valArr => {
//   let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
//   return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
// };

// const transposeMatrix = matrix => {
//   return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
// };

export const DatasetService = {
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
  downloadDatasetFileData,
  downloadExportDatasetFile,
  downloadExportFile,
  downloadFileData,
  downloadReferenceDatasetFileData,
  errorStatisticsById,
  exportDataById,
  exportDatasetDataExternal,
  exportTableDataById,
  exportTableSchemaById,
  getMetaData,
  getReferencedFieldValues,
  groupedErrorsById,
  orderFieldSchema,
  orderTableSchema,
  schemaById,
  tableDataById,
  updateDatasetFeedbackStatus,
  updateDatasetSchemaDesign,
  updateFieldById,
  updateRecordFieldDesign,
  updateRecordsById,
  updateReferenceDatasetStatus,
  updateSchemaNameById,
  updateTableDescriptionDesign,
  updateTableNameDesign,
  validateDataById,
  validateSqlRules
};
