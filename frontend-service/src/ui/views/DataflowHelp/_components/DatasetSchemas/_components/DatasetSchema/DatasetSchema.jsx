import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

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
        const existACodelist = tableDTO.records[0].fields.filter(field => field.type === 'CODELIST');
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          if (!isEmpty(existACodelist)) {
            return {
              name: fieldDTO.name,
              type: fieldDTO.type,
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-',
              codelist: fieldDTO.type === 'CODELIST' ? 'Future codelist name, description and version or whatever.' : ''
            };
          } else {
            return {
              name: fieldDTO.name,
              type: fieldDTO.type,
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-'
            };
          }
        });
        table.fields = fields;
      }

      return table;
    });

    parsedDataset.tables = tables;
  }
  // parsedDataset.codelists = getCodelists(1);

  // parsedDataset.codelists = {
  //   id: 18,
  //   name: 'cl2',
  //   description: 'code1',
  //   category: {
  //     id: 1,
  //     shortCode: 'c1',
  //     description: 'cat1'
  //   },
  //   version: 1,
  //   items: [
  //     {
  //       id: 16,
  //       shortCode: 'o',
  //       label: 'two',
  //       definition: 'def',
  //       codelistId: null
  //     }
  //   ],
  //   status: 'DESIGN'
  // };

  const dataset = {};
  dataset[design.datasetSchemaName] = parsedDataset;
  return dataset;
};

export { DatasetSchema };
