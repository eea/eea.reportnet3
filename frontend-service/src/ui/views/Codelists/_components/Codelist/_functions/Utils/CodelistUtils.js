const getItem = (items, selectedItem) => {
  return items.filter(item => item.itemId === selectedItem.itemId)[0];
};

export const CodelistUtils = {
  getItem
};
