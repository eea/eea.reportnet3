const getIndexByHeader = (header, tabsArray) => {
  return tabsArray
    .map(tab => {
      return tab.header;
    })
    .indexOf(header);
};

const getIndexByTableProperty = (value, tabsArray, property) => {
  const indx = tabsArray
    .map(tab => {
      return tab[property];
    })
    .indexOf(value);
  return indx !== -1 ? indx : 0;
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
