import React from 'react';

import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, index, validationList }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      const parsedDesignDataset = parseDesignDataset(designDataset, validationList);

      const columnOptions = {
        fields: {
          filtered: false,
          groupable: true,
          names: { shortCode: 'Shortcode', codelistItems: 'Codelist items' }
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
              automatic: [{ label: 'True', value: 'true' }, { label: 'False', value: 'false' }],
              enabled: [{ label: 'True', value: 'true' }, { label: 'False', value: 'false' }],
              levelError: [
                { label: 'Info', value: 'INFO' },
                { label: 'Warning', value: 'WARNING' },
                { label: 'Error', value: 'ERROR' },
                { label: 'Blocker', value: 'BLOCKER' }
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
      if (!isNull(tableDTO.records) && !isNil(tableDTO.records[0].fields) && tableDTO.records[0].fields.length > 0) {
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          const field = {};
          field.description = !isNull(fieldDTO.description) ? fieldDTO.description : '-';
          field.name = fieldDTO.name;
          field.type = fieldDTO.type;
          if (fieldDTO.type === 'CODELIST') {
            field.codelistItems = fieldDTO.codelistItems;
          }
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
