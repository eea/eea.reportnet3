import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { DatasetRepository } from 'repositories/DatasetRepository';
import { ValidationRepository } from 'repositories/ValidationRepository';

import { DatasetUtils } from 'services/_utils/DatasetUtils';

import { Dataset } from 'entities/Dataset';
import { DatasetError } from 'entities/DatasetError';
import { DatasetTable } from 'entities/DatasetTable';
import { DatasetTableField } from 'entities/DatasetTableField';
import { DatasetTableRecord } from 'entities/DatasetTableRecord';
import { Validation } from 'entities/Validation';

import { CoreUtils } from 'repositories/_utils/CoreUtils';

export const DatasetService = {
  createRecordDesign: async (datasetId, datasetTableRecordField) => {
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

    return await DatasetRepository.createRecordDesign(datasetId, datasetTableFieldDesign);
  },

  createRecord: async (datasetId, tableSchemaId, records) => {
    const datasetTableRecords = [];
    records.forEach(record => {
      let fields = record.dataRow.map(dataTableFieldDTO => {
        let newField = new DatasetTableField({});
        newField.id = null;
        newField.idFieldSchema = dataTableFieldDTO.fieldData.fieldSchemaId;
        newField.type = dataTableFieldDTO.fieldData.type;
        newField.value = DatasetUtils.parseValue(
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

    return await DatasetRepository.createRecord(datasetId, tableSchemaId, datasetTableRecords);
  },

  createTableDesign: async (datasetId, tableSchemaName) =>
    await DatasetRepository.createTableDesign(datasetId, tableSchemaName),

  deleteData: async datasetId => await DatasetRepository.deleteData(datasetId),

  deleteAttachment: async (datasetId, fieldId) => await DatasetRepository.deleteAttachment(datasetId, fieldId),

  deleteFieldDesign: async (datasetId, recordId) => await DatasetRepository.deleteFieldDesign(datasetId, recordId),

  deleteRecord: async (datasetId, recordId, deleteInCascade) =>
    await DatasetRepository.deleteRecord(datasetId, recordId, deleteInCascade),

  deleteSchema: async datasetId => await DatasetRepository.deleteSchema(datasetId),

  deleteTableData: async (datasetId, tableId) => await DatasetRepository.deleteTableData(datasetId, tableId),

  deleteTableDesign: async (datasetId, tableSchemaId) =>
    await DatasetRepository.deleteTableDesign(datasetId, tableSchemaId),

  downloadExportDatasetFile: async (datasetId, fileName) =>
    await DatasetRepository.downloadExportDatasetFile(datasetId, fileName),

  downloadExportFile: async (datasetId, fileName, providerId) =>
    await DatasetRepository.downloadExportFile(datasetId, fileName, providerId),

  downloadFileData: async (dataflowId, datasetId, fieldId, dataProviderId) =>
    await DatasetRepository.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId),

  downloadPublicDatasetFile: async (dataflowId, dataProviderId, fileName) =>
    await DatasetRepository.downloadPublicDatasetFile(dataflowId, dataProviderId, fileName),

  downloadPublicReferenceDatasetFileData: async (dataflowId, fileName) =>
    await DatasetRepository.downloadPublicReferenceDatasetFileData(dataflowId, fileName),

  getStatistics: async (datasetId, tableSchemaNames) => {
    const datasetTablesDTO = await DatasetRepository.getStatistics(datasetId);

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
      ? DatasetUtils.tableStatisticValuesWithErrors(tableStatisticValues)
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
  },

  exportDatasetData: async (datasetId, fileType) => await DatasetRepository.exportDatasetData(datasetId, fileType),

  exportDatasetDataExternal: async (datasetId, integrationId) =>
    await DatasetRepository.exportDatasetDataExternal(datasetId, integrationId),

  exportTableData: async (datasetId, tableSchemaId, fileType) =>
    await DatasetRepository.exportTableData(datasetId, tableSchemaId, fileType),

  exportTableSchema: async (datasetId, datasetSchemaId, tableSchemaId, fileType) =>
    await DatasetRepository.exportTableSchema(datasetId, datasetSchemaId, tableSchemaId, fileType),

  getMetadata: async datasetId => {
    const datasetTableDataDTO = await DatasetRepository.getMetadata(datasetId);
    return new Dataset({
      datasetFeedbackStatus:
        !isNil(datasetTableDataDTO.data.status) && capitalize(datasetTableDataDTO.data.status.split('_').join(' ')),
      datasetSchemaId: datasetTableDataDTO.data.datasetSchema,
      datasetSchemaName: datasetTableDataDTO.data.dataSetName,
      dataProviderId: datasetTableDataDTO.data.dataProviderId
    });
  },

  getReferencedFieldValues: async (
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
    return referencedFieldValuesDTO.data.map(
      referencedFieldDTO =>
        new DatasetTableField({
          fieldId: referencedFieldDTO.id,
          fieldSchemaId: referencedFieldDTO.idFieldSchema,
          label: referencedFieldDTO.label,
          type: referencedFieldDTO.type,
          value: referencedFieldDTO.value
        })
    );
  },

  getShowValidationErrors: async (
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
    const datasetErrorsDTO = await DatasetRepository.getShowValidationErrors(
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
    return dataset;
  },

  updateFieldOrder: async (datasetId, position, fieldSchemaId) =>
    await DatasetRepository.updateFieldOrder(datasetId, position, fieldSchemaId),

  updateTableOrder: async (datasetId, position, tableSchemaId) =>
    await DatasetRepository.updateTableOrder(datasetId, position, tableSchemaId),

  getSchema: async datasetId => {
    const datasetSchemaDTO = await DatasetRepository.getSchema(datasetId);
    const rulesDTO = await ValidationRepository.getAll(datasetSchemaDTO.data.idDataSetSchema);

    const dataset = new Dataset({
      availableInPublic: datasetSchemaDTO.data.availableInPublic,
      datasetSchemaDescription: datasetSchemaDTO.data.description,
      datasetSchemaId: datasetSchemaDTO.data.idDataSetSchema,
      datasetSchemaName: datasetSchemaDTO.data.nameDatasetSchema,
      levelErrorTypes:
        !isUndefined(rulesDTO.data) && rulesDTO.data !== ''
          ? DatasetUtils.getAllLevelErrorsFromRuleValidations(rulesDTO.data)
          : [],
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
    return dataset;
  },

  getTableData: async ({
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
    const tableDataDTO = await DatasetRepository.getTableData(
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
          value: DatasetUtils.parseValue(DataTableFieldDTO.type, DataTableFieldDTO.value)
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
    return table;
  },

  updateField: async (datasetId, fieldSchemaId, fieldId, fieldType, fieldValue, updateInCascade) => {
    const datasetTableField = new DatasetTableField({});
    datasetTableField.id = fieldId;
    datasetTableField.idFieldSchema = fieldSchemaId;
    datasetTableField.type = fieldType;
    datasetTableField.value = DatasetUtils.parseValue(fieldType, fieldValue, true);

    return await DatasetRepository.updateField(datasetId, datasetTableField, updateInCascade);
  },

  updateFieldDesign: async (datasetId, record) => {
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

    return await DatasetRepository.updateFieldDesign(datasetId, datasetTableFieldDesign);
  },

  updateRecord: async (datasetId, record, updateInCascade) => {
    const fields = record.dataRow.map(dataTableFieldDTO => {
      let newField = new DatasetTableField({});
      newField.id = dataTableFieldDTO.fieldData.id;
      newField.idFieldSchema = dataTableFieldDTO.fieldData.fieldSchemaId;
      newField.type = dataTableFieldDTO.fieldData.type;
      newField.value = DatasetUtils.parseValue(
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
    return await DatasetRepository.updateRecord(datasetId, [datasetTableRecord], updateInCascade);
  },

  updateReferenceDatasetStatus: async (datasetId, updatable) =>
    await DatasetRepository.updateReferenceDatasetStatus(datasetId, updatable),

  updateDatasetFeedbackStatus: async (dataflowId, datasetId, message, feedbackStatus) =>
    await DatasetRepository.updateDatasetFeedbackStatus(
      dataflowId,
      datasetId,
      message,
      feedbackStatus.toUpperCase().split(' ').join('_')
    ),

  updateDatasetDesign: async (datasetId, datasetSchema) =>
    await DatasetRepository.updateDatasetDesign(datasetId, datasetSchema),

  updateDatasetNameDesign: async (datasetId, datasetSchemaName) =>
    await DatasetRepository.updateDatasetNameDesign(datasetId, datasetSchemaName),

  updateTableDesign: async (
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
  ) =>
    await DatasetRepository.updateTableDesign(
      tableSchemaToPrefill,
      tableSchemaId,
      tableSchemaDescription,
      tableSchemaIsReadOnly,
      datasetId,
      tableSchemaNotEmpty,
      tableSchemaFixedNumber
    ),

  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) =>
    await DatasetRepository.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId),

  validate: async datasetId => await DatasetRepository.validate(datasetId),

  validateSqlRules: async (datasetId, datasetSchemaId) =>
    await DatasetRepository.validateSqlRules(datasetId, datasetSchemaId)
};
