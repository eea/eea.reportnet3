import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';

import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';

const getWebformTabs = (allTables = [], schemaTables, configTables = {}) => {
  const initialValues = {};

  let tableIdx = 0;
  if (QuerystringUtils.getUrlParamValue('tab') !== '') {
    const filteredTable = schemaTables.filter(
      schemaTable => schemaTable.id === QuerystringUtils.getUrlParamValue('tab')
    );
    if (!isEmpty(filteredTable)) {
      tableIdx = allTables.indexOf(filteredTable[0].name);
    }
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

export const Article15Utils = { getWebformTabs };
