import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import cloneDeep from 'lodash/cloneDeep';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table => {
      if (!isUndefined(table.records) && !table.addTab) {
        table.records.forEach(record =>
          record.fields.forEach(field => {
            if (
              !isNil(field) &&
              TextUtils.areEquals(field.type, 'LINK') &&
              !isNil(field.referencedField) &&
              !isNil(field.referencedField.name)
            ) {
              if (
                !isNil(field) &&
                !isNil(field.referencedField) &&
                field.referencedField.referencedField.fieldSchemaId === fieldPkId
              ) {
                referencedFields++;
              }
            } else {
              if (
                !isNil(field) &&
                TextUtils.areEquals(field.type, 'LINK') &&
                !isNil(field.referencedField) &&
                field.referencedField.idPk === fieldPkId
              ) {
                referencedFields++;
              }
            }
          })
        );
      }
    })
  );
  return referencedFields;
};

const getIndexById = (datasetSchemaId, datasetSchemasArray) => {
  return datasetSchemasArray.map(datasetSchema => datasetSchema.datasetSchemaId).indexOf(datasetSchemaId);
};

const getTabs = ({ datasetSchema, datasetStatistics, editable, isDataflowOpen, isDesignDatasetEditorRead }) => {
  const inmDatasetSchema = cloneDeep(datasetSchema);
  inmDatasetSchema.tables?.forEach((table, idx) => {
    table.addTab = false;
    table.description = table.description || table.tableSchemaDescription;
    table.editable = editable;
    table.fixedNumber = table.fixedNumber || table.tableSchemaFixedNumber;
    table.hasErrors =
      !isNil(datasetStatistics) && !isEmpty(datasetStatistics)
        ? {
            ...datasetStatistics.tables.filter(tab => tab['tableSchemaId'] === table['tableSchemaId'])[0]
          }.hasErrors
        : false;
    table.header = table.tableSchemaName;
    table.index = idx;
    table.levelErrorTypes = inmDatasetSchema.levelErrorTypes;
    table.newTab = false;
    table.notEmpty = table.notEmpty || table.tableSchemaNotEmpty;
    table.readOnly = table.readOnly || table.tableSchemaReadOnly;
    table.showContextMenu = false;
    table.toPrefill = table.toPrefill || table.tableSchemaToPrefill;
  });
  //Add tab Button/Tab and filter for undefined tableSchemaId tables (webform)
  inmDatasetSchema.tables = inmDatasetSchema.tables?.filter(
    table => table.tableSchemaId !== undefined && table.addTab === false && table.tableSchemaId !== ''
  );
  if (!isDataflowOpen && !isDesignDatasetEditorRead) {
    inmDatasetSchema.tables?.push({ header: '+', editable: false, addTab: true, newTab: false, index: -1 });
  }
  return inmDatasetSchema.tables;
};

const getValidExtensions = (validExtensions = '') => {
  return validExtensions
    ?.split(/,\s*/)
    .map(ext => `.${ext}`)
    .join(',');
};

const getValidExtensionsTooltip = (validExtensions = '') => {
  return validExtensions
    ?.split(/,\s*/)
    .map(ext => ` .${ext}`)
    .join(',');
};

export const DatasetDesignerUtils = {
  getCountPKUseInAllSchemas,
  getIndexById,
  getTabs,
  getValidExtensions,
  getValidExtensionsTooltip
};
