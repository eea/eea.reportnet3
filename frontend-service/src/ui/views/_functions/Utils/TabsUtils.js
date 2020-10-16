const getIndexByHeader = (header, tabsArray) => {
  return tabsArray
    .map(tab => {
      return tab.header;
    })
    .indexOf(header);
};

const getIndexByTableProperty = (value, tabsArray, property) => {
  return tabsArray
    .map(tab => {
      return tab[property];
    })
    .indexOf(value);
};

const getMaxIndex = tabsArray => {
  return Math.max(...tabsArray.map(tab => tab.index));
};

const getTableSchemaIdByIndex = (index, tabsArray) => {
  if (index === '') return -1;
  return tabsArray[index].id;
};

export const TabsUtils = {
  getIndexByHeader,
  getIndexByTableProperty,
  getMaxIndex,
  getTableSchemaIdByIndex
};
