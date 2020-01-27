import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, codelistsList, index }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      let parsedDesignDataset = parseDesignDataset(designDataset, codelistsList);
      designDataset.codelistItems = parsedDesignDataset[designDataset.datasetSchemaName].codelists;
      return (
        <div>
          <TreeView
            excludeBottomBorder={false}
            groupableProperties={['fields', 'codelists']}
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

const parseDesignDataset = (design, codelistsListWithSchema) => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;
  let codelistItemsData = [];
  let codelistsBySchema = [];
  if (!isUndefined(codelistsListWithSchema)) {
    codelistsBySchema = codelistsListWithSchema.find(
      x => x.schema.datasetSchemaName.toString() === design.datasetSchemaName.toString()
    );
    if (!isUndefined(codelistsBySchema)) {
      codelistsBySchema = codelistsBySchema.codelists;
    }
  }

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
              if (!isUndefined(codelistsListWithSchema) && !isEmpty(codelistsBySchema)) {
                let codelist = codelistsBySchema.find(codelist => codelist.id === fieldDTO.codelistId);
                if (!isUndefined(codelist)) {
                  let codelistView = [];
                  fieldCodelist = `${codelist.name} (${codelist.version})`;
                  // codelists.name = fieldCodelist;
                  if (!isEmpty(codelist) && !isEmpty(codelist.items)) {
                    codelist.items.forEach(itemDTO => {
                      let isRepeatedCodelistItem = codelistItemsData.filter(item => item.id === itemDTO.id);
                      if (!isUndefined(isRepeatedCodelistItem) && isRepeatedCodelistItem.length > 0) {
                        return;
                      }
                      let codelistItemView = {};
                      codelistItemView.shortCode = itemDTO.shortCode;
                      codelistItemView.label = itemDTO.definition;
                      codelistItemView.definition = itemDTO.definition;
                      codelistView.push(codelistItemView);
                    });
                  }
                  parsedDataset[fieldCodelist] = codelistView;
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
  }
  const dataset = {};
  dataset[design.datasetSchemaName] = parsedDataset;
  return dataset;
};

export { DatasetSchema };
