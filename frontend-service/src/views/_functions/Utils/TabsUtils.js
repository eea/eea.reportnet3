const getIndexByHeader = (header, tabsArray) => tabsArray.map(tab => tab.header).indexOf(header);

const getIndexByTableProperty = (value, tabsArray, property) => {
  const indx = tabsArray.map(tab => tab[property]).indexOf(value);
  return indx !== -1 ? indx : 0;
};

const getMaxIndex = tabsArray => Math.max(...tabsArray.map(tab => tab.index));

const getTableSchemaIdByIndex = (index, tabsArray) => {
  if (index === '') {
    return -1;
  }

  return tabsArray[index].id;
};

export const TabsUtils = {
  getIndexByHeader,
  getIndexByTableProperty,
  getMaxIndex,
  getTableSchemaIdByIndex
};
