const getItem = (items, selectedItem) => {
  return items.filter(item => item.id === selectedItem.id)[0];
};

export const CodelistUtils = {
  getItem
};
