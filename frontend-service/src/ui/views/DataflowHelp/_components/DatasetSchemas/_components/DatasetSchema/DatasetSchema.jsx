import React from 'react';

import { isUndefined, isNull } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, index }) => {
  const renderDatasetSchema = () => {
    return !isUndefined(designDataset) && !isNull(designDataset) ? (
      <div>
        <TreeView
          excludeBottomBorder={false}
          groupableProperties={['fields']}
          key={index}
          property={parseDesignDataset(designDataset)}
          propertyName={''}
          rootProperty={''}
        />
      </div>
    ) : null;
  };

  return renderDatasetSchema();
};

const parseDesignDataset = design => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;

  if (!isUndefined(design.tables) && !isNull(design.tables) && design.tables.length > 0) {
    const tables = design.tables.map(tableDTO => {
      const table = {};
      table.tableSchemaName = tableDTO.tableSchemaName;
      table.tableSchemaDescription = tableDTO.tableSchemaDescription;
      if (
        !isNull(tableDTO.records) &&
        !isUndefined(tableDTO.records[0].fields) &&
        !isNull(tableDTO.records[0].fields) &&
        tableDTO.records[0].fields.length > 0
      ) {
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          return {
            name: fieldDTO.name,
            type: fieldDTO.type,
            description: !isNull(fieldDTO.description) ? fieldDTO.description : '-'
          };
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
