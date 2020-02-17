import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ designDataset, codelistsList, index }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      const parsedDesignDataset = parseDesignDataset(designDataset, codelistsList);
      const codelistNames = parseCodelistList(codelistsList, designDataset);

      const codelistTitles = [];
      if (!isUndefined(codelistNames)) {
        codelistNames.forEach(name => {
          codelistTitles.push(name);
        });
      }

      const groupableProperties = ['fields'].concat(codelistTitles);

      return (
        <div>
          <TreeView
            excludeBottomBorder={false}
            groupableProperties={groupableProperties}
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

const parseCodelistList = (codelistsList, designDataset) => {
  if (isUndefined(codelistsList)) {
    return;
  }
  const codelistsSchema = codelistsList.filter(
    codelistList => codelistList.schema.datasetSchemaName === designDataset.datasetSchemaName
  );
  if (isUndefined(codelistsSchema)) {
    return;
  }
  const codelistNames = [];
  codelistsSchema.forEach(codelistList => {
    codelistList.codelists.forEach(codelist => {
      let title = `${codelist.name} (${codelist.version})`;
      codelistNames.push(title);
    });
  });
  return codelistNames;
};

const parseDesignDataset = (design, codelistsListWithSchema) => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;
  const codelistItemsData = [];
  let codelistsBySchema = [];
  if (!isUndefined(codelistsListWithSchema)) {
    codelistsBySchema = codelistsListWithSchema.find(x => x.schema.datasetSchemaName === design.datasetSchemaName);
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
                  if (!isEmpty(codelist) && !isEmpty(codelist.items)) {
                    codelist.items.forEach(itemDTO => {
                      let isRepeatedCodelistItem = codelistItemsData.filter(item => item.id === itemDTO.id);
                      if (!isUndefined(isRepeatedCodelistItem) && isRepeatedCodelistItem.length > 0) {
                        return;
                      }
                      let codelistItemView = {};
                      codelistItemView.shortCode = itemDTO.shortCode;
                      codelistItemView.label = itemDTO.label;
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
