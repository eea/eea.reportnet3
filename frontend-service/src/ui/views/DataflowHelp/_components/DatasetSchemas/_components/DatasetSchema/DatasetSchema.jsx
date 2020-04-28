import React from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetSchema.module.scss';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, index, validationList }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      const parsedDesignDataset = parseDesignDataset(designDataset, validationList);

      const columnOptions = {
        levelErrorTypes: {
          hasClass: true,
          class: styles.levelError,
          subClasses: [styles.blocker, styles.error, styles.warning, styles.info]
        },
        fields: {
          filtered: false,
          groupable: true,
          names: { shortCode: 'Shortcode', codelistItems: 'Single select items' }
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
            entityType: 'Entity type',
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

  return renderDatasetSchema();
};

// const getMultiselectValues = (validations, field) => {
//   if (!isUndefined(validations)) {
//     console.log(
//       [...new Set(validations.map(validation => validation[field]))].map(fieldValue => {
//         return { label: fieldValue, value: fieldValue };
//       })
//     );
//   }
// };

const getFieldFormat = fieldType => {
  switch (fieldType.toUpperCase()) {
    case 'DATE':
      return 'YYYY-MM-DD';
    case 'TEXT':
      return '5000 characters';
    default:
      return '';
  }
};

const parseDesignDataset = (design, validationList) => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;
  parsedDataset.validations = validationList;
  if (!isUndefined(design.tables) && !isNull(design.tables) && design.tables.length > 0) {
    const tables = design.tables.map(tableDTO => {
      const table = {};
      table.tableSchemaName = tableDTO.tableSchemaName;
      table.tableSchemaDescription = tableDTO.tableSchemaDescription;
      table.tableSchemaReadOnly = tableDTO.tableSchemaReadOnly;
      table.tableSchemaToPrefill = !isNil(tableDTO.tableSchemaToPrefill);
      if (!isNull(tableDTO.records) && !isNil(tableDTO.records[0].fields) && tableDTO.records[0].fields.length > 0) {
        const containsCodelists = !isEmpty(
          tableDTO.records[0].fields.filter(fieldElmt => fieldElmt.type === 'CODELIST')
        );
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          const field = {};
          field.name = fieldDTO.name;
          field.pk = fieldDTO.pk;
          field.required = fieldDTO.required;
          field.description = !isNull(fieldDTO.description) ? fieldDTO.description : '-';
          field.type = fieldDTO.type;
          if (containsCodelists) {
            if (fieldDTO.type === 'CODELIST') {
              field.codelistItems = fieldDTO.codelistItems;
            } else {
              field.codelistItems = [];
            }
          }
          field.format = getFieldFormat(fieldDTO.type);
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

export { DatasetSchema };
