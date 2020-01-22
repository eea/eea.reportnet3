import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, codelistsList, index }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      let parsedDesignDataset = parseDesignDataset(designDataset, codelistsList);
      designDataset.codelistItems = parsedDesignDataset[designDataset.datasetSchemaName].codelistItems;
      return (
        <div>
          <TreeView
            excludeBottomBorder={false}
            groupableProperties={['fields', 'codelistItems']}
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

const parseDesignDataset = (design, codelistsList) => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;
  let codelistItems = [];

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
            let fieldCodelist;
            if (fieldDTO.type === 'CODELIST') {
              if (!isUndefined(codelistsList)) {
                let codelist = codelistsList.find(codelist => codelist.id === fieldDTO.codelistId);
                if (!isUndefined(codelist)) {
                  fieldCodelist = `${codelist.name} (v${codelist.version})`;
                  if (!isEmpty(codelist.items)) {
                    codelist.items.forEach(itemDTO => {
                      let codelistItem = {};
                      codelistItem.definition = itemDTO.definition;
                      codelistItem.label = itemDTO.definition;
                      codelistItem.shortCode = itemDTO.shortCode;
                      codelistItems.push(codelistItem);
                    });
                  }
                }
              }
            }
            return {
              name: fieldDTO.name,
              type: fieldDTO.type,
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-',
              codelist: !isNull(fieldCodelist) ? fieldCodelist : ''
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
    parsedDataset.codelistItems = codelistItems;
  }
  const dataset = {};
  dataset[design.datasetSchemaName] = parsedDataset;
  return dataset;
};

export { DatasetSchema };
