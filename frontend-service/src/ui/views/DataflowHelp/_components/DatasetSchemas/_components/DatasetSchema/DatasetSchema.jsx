import React, { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetSchema.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, index, extensionsOperationsList = [], uniqueList = [], validationList }) => {
  const resources = useContext(ResourcesContext);
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      const parsedDesignDataset = parseDesignDataset(
        designDataset,
        extensionsOperationsList,
        uniqueList,
        validationList
      );

      const columnOptions = {
        levelErrorTypes: {
          hasClass: true,
          class: styles.levelError,
          subClasses: [styles.blocker, styles.error, styles.warning, styles.info]
        },
        properties: {
          filtered: false,
          groupable: true,
          names: {
            description: 'Description',
            readOnly: 'Read only',
            prefilled: 'Prefilled',
            mandatory: 'Mandatory',
            fixedNumber: 'Fixed number of rows'
          }
        },
        fields: {
          filtered: false,
          groupable: true,
          names: {
            shortCode: 'Shortcode',
            codelistItems: 'Single select items',
            pk: 'Primary key',
            readOnly: 'Read only'
          }
        },
        extensionsOperations: {
          filtered: true,
          groupable: true,
          narrow: true,
          invisible: ['datasetSchemaId'],
          names: {
            operation: 'Operation',
            fileExtension: 'Extension'
          }
        },
        uniques: {
          filtered: true,
          groupable: true,
          narrow: true,
          invisible: ['datasetSchemaId'],
          names: {
            tableName: 'Table',
            fieldName: 'Field'
          }
        },
        validations: {
          filtered: true,
          filterType: {
            multiselect: {
              entityType: [
                { label: 'Field', value: 'FIELD' },
                { label: 'Record', value: 'RECORD' },
                { label: 'Table', value: 'TABLE' },
                { label: 'Dataset', value: 'DATASET' }
              ],
              automatic: [
                { label: 'True', value: 'true' },
                { label: 'False', value: 'false' }
              ],
              enabled: [
                { label: 'True', value: 'true' },
                { label: 'False', value: 'false' }
              ],
              levelError: [
                { label: 'Info', value: 'INFO', class: styles.levelError, subclass: styles.info },
                { label: 'Warning', value: 'WARNING', class: styles.levelError, subclass: styles.warning },
                { label: 'Error', value: 'ERROR', class: styles.levelError, subclass: styles.error },
                { label: 'Blocker', value: 'BLOCKER', class: styles.levelError, subclass: styles.blocker }
              ]
            }
          },
          groupable: true,
          invisible: ['datasetSchemaId', 'id'],
          names: {
            tableName: 'Table',
            fieldName: 'Field',
            entityType: 'Type of qc',
            levelError: 'Level error',
            ruleName: 'Rule name'
          }
        }
      };
      return (
        <div>
          <TreeView
            columnOptions={columnOptions}
            excludeBottomBorder={false}
            key={index}
            property={parsedDesignDataset}
            propertyName={''}
            rootProperty={''}
          />
        </div>
      );
    } else {
      return null;
    }
  };

  const getFieldFormat = field => {
    switch (field.type.toUpperCase()) {
      case 'DATE':
        return resources.messages['dateFieldFormatRestriction'];
      case 'TEXT':
        return resources.messages['textFieldFormatRestriction'];
      case 'RICH_TEXT':
        return resources.messages['richTextFieldFormatRestriction'];
      case 'NUMBER_INTEGER':
        return resources.messages['longFieldFormatRestriction'];
      case 'NUMBER_DECIMAL':
        return resources.messages['decimalFieldFormatRestriction'];
      case 'EMAIL':
        return resources.messages['emailFieldFormatRestriction'];
      case 'PHONE':
        return resources.messages['phoneNumberFieldFormatRestriction'];
      case 'URL':
        return resources.messages['urlFieldFormatRestriction'];
      case 'ATTACHMENT':
        return `${resources.messages['validExtensions']} ${field.validExtensions.join(', ')}
        - ${resources.messages['maxFileSize']} ${
          field.maxSize.toString() !== '0'
            ? `${field.maxSize} ${resources.messages['MB']}`
            : resources.messages['maxSizeNotDefined']
        }`;
      default:
        return '';
    }
  };

  const parseDesignDataset = (design, extensionsOperationsList, uniqueList, validationList) => {
    const parsedDataset = {};
    parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
    parsedDataset.levelErrorTypes = design.levelErrorTypes;
    parsedDataset.extensionsOperations = extensionsOperationsList;
    parsedDataset.uniques = uniqueList;
    parsedDataset.validations = validationList;
    if (!isUndefined(design.tables) && !isNull(design.tables) && design.tables.length > 0) {
      const tables = design.tables.map(tableDTO => {
        const tableProperties = [
          {
            description:
              !isNil(tableDTO.tableSchemaDescription) && tableDTO.tableSchemaDescription !== ''
                ? tableDTO.tableSchemaDescription
                : '-',
            readOnly: tableDTO.tableSchemaReadOnly,
            prefilled: !isNil(tableDTO.tableSchemaToPrefill) ? tableDTO.tableSchemaToPrefill : false,
            fixedNumber: !isNil(tableDTO.tableSchemaFixedNumber) ? tableDTO.tableSchemaFixedNumber : false,
            mandatory: tableDTO.tableSchemaNotEmpty
          }
        ];

        const table = {};
        table.tableSchemaName = tableDTO.tableSchemaName;
        table.properties = tableProperties;
        if (!isNull(tableDTO.records) && !isNil(tableDTO.records[0].fields) && tableDTO.records[0].fields.length > 0) {
          const containsCodelists = !isEmpty(
            tableDTO.records[0].fields.filter(
              fieldElmt => fieldElmt.type === 'CODELIST' || fieldElmt.type === 'MULTISELECT_CODELIST'
            )
          );
          const fields = tableDTO.records[0].fields.map(fieldDTO => {
            const field = {};
            field.pk = fieldDTO.pk;
            field.readOnly = fieldDTO.readOnly;
            field.required = fieldDTO.required;
            field.name = fieldDTO.name;
            field.description = !isNull(fieldDTO.description) ? fieldDTO.description : '-';
            field.type = fieldDTO.type;
            if (containsCodelists) {
              if (fieldDTO.type === 'CODELIST' || fieldDTO.type === 'MULTISELECT_CODELIST') {
                field.codelistItems = fieldDTO.codelistItems;
              } else {
                field.codelistItems = [];
              }
            }
            field.format = getFieldFormat(fieldDTO);
            return field;
          });
          table.fields = fields;
        }

        return table;
      });
      parsedDataset.tables = tables;
    }
    const dataset = {};
    dataset[design.datasetSchemaName] = parsedDataset;
    return dataset;
  };

  return renderDatasetSchema();
};

export { DatasetSchema };
