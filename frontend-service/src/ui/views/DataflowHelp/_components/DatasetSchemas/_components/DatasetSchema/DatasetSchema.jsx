import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

const DatasetSchema = ({ codelistsList, designDataset, index, validationList }) => {
  const renderDatasetSchema = () => {
    if (!isUndefined(designDataset) && !isNull(designDataset)) {
      const parsedDesignDataset = parseDesignDataset(designDataset, codelistsList, validationList);
      console.log({ parsedDesignDataset });
      const codelistNames = parseCodelistList(codelistsList, designDataset);
      //  const validationNames = parseValidationList(validationList, designDataset)

      const codelistTitles = [];
      if (!isUndefined(codelistNames)) {
        codelistNames.forEach(name => {
          codelistTitles.push(name);
        });
      }

      const columnOptions = {
        fields: { filtered: false, groupable: true, names: { shortCode: 'Shortcode' } },
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
          names: { entityType: 'Entity type', levelError: 'Level error', ruleName: 'Rule name' }
        }
      };
      codelistTitles.forEach(codelistTitle => (columnOptions[codelistTitle] = { groupable: true }));
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

const parseValidationList = (validationList, designDataset) => {
  if (isUndefined(validationList)) {
    return;
  }
  const schemaValidations = validationList.filter(
    validation => validation.datasetSchemaId === designDataset.datasetSchemaId
  );
  if (isUndefined(schemaValidations)) {
    return;
  }
  return schemaValidations.map(validation => `${validation.ruleName}`);
};

const parseDesignDataset = (design, codelistsListWithSchema, validationList) => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;
  parsedDataset.codelists = [];
  parsedDataset.validations = validationList;
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
                const codelist = codelistsBySchema.find(codelist => codelist.id === fieldDTO.codelistId);
                if (!isUndefined(codelist)) {
                  const codelistView = [];
                  fieldCodelist = `${codelist.name} (${codelist.version})`;
                  if (!isEmpty(codelist) && !isEmpty(codelist.items)) {
                    codelist.items.forEach(itemDTO => {
                      const isRepeatedCodelistItem = codelistItemsData.filter(item => item.id === itemDTO.id);
                      if (!isUndefined(isRepeatedCodelistItem) && isRepeatedCodelistItem.length > 0) {
                        return;
                      }
                      const codelistItemView = {};
                      codelistItemView.shortCode = itemDTO.shortCode;
                      codelistItemView.label = itemDTO.label;
                      codelistItemView.definition = itemDTO.definition;
                      codelistView.push(codelistItemView);
                    });
                  }
                  parsedDataset.codelists.push({ [fieldCodelist]: codelistView });
                }
              }
            }
            return {
              codelist: !isNull(fieldCodelist) ? fieldCodelist : '',
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-',
              name: fieldDTO.name,
              type: fieldDTO.type
            };
          } else {
            return {
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-',
              name: fieldDTO.name,
              type: fieldDTO.type
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
