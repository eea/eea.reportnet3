const getItem = (items, selectedItem) => {
  return items.filter(item => item.id === selectedItem.id)[0];
};

const getItemByIndex = (items, itemId) => {
  return items
    .map(e => {
      return e.id;
    })
    .indexOf(itemId);
};

export const CodelistUtils = {
  getItem,
  getItemByIndex
};
