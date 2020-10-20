import compact from 'lodash/compact';

import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';

const getWebformTabs = (allTables = [], schemaTables, configTables = {}) => {
  const initialValues = {};

  let tableIdx = 0;
  if (QuerystringUtils.getUrlParamValue('tab') !== '') {
    console.log('schemaTables', schemaTables);
    const filteredTable = schemaTables.filter(
      schemaTable => schemaTable.id === QuerystringUtils.getUrlParamValue('tab')
    );
    tableIdx = allTables.indexOf(filteredTable[0].name);

    //Search on subtables for parent id
    if (tableIdx === -1) {
      configTables.forEach(table => {
        table.elements.forEach(element => {
          if (element.type === 'TABLE') {
            if (element.name === filteredTable[0].name) {
              tableIdx = allTables.indexOf(table.name);
            }
          }
        });
      });
    }
  }
  const value = allTables[tableIdx === -1 ? 0 : tableIdx];

  compact(allTables).forEach(table => {
    initialValues[table] = false;
    initialValues[value] = true;
  });

  return initialValues;
};

const mergeArrays = (array1 = [], array2 = [], array1Key = '', array2Key = '') => {
  const result = [];
  for (let i = 0; i < array1.length; i++) {
    result.push({ ...array1[i], ...array2.find(element => element[array2Key] === array1[i][array1Key]) });
  }
  return result;
};

export const Article15Utils = { getWebformTabs, mergeArrays };
