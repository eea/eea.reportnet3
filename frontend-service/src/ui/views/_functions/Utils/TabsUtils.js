import { isNil } from 'lodash';

const getIndexByHeader = (header, tabsArray) => {
  return tabsArray
    .map(tab => {
      return tab.header;
    })
    .indexOf(header);
};

const getIndexByTableSchemaId = (tableSchemaId, tabsArray) => {
  return tabsArray
    .map(tab => {
      return tab.tableSchemaId;
    })
    .indexOf(tableSchemaId);
};

const getMaxIndex = tabsArray => {
  return Math.max(...tabsArray.map(tab => tab.index));
};

export const TabsUtils = {
  getIndexByHeader,
  getIndexByTableSchemaId,
  getMaxIndex
};
