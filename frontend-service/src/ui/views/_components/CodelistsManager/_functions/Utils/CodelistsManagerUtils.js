const parseKey = item => item.toString().toUpperCase();

const filterByText = (data, filterText) => {
  const filteredData = data.filter(function f(o) {
    if (o.shortCode && parseKey(o.shortCode).includes(filterText)) return true;
    if (o.name && parseKey(o.name).includes(filterText)) return true;
    if (o.description && parseKey(o.description).includes(filterText)) return true;
    if (o.version && parseKey(o.version).includes(filterText)) return true;
    // if (o.status && parseKey(o.status).includes(filterText)) return true;
    if (o.code && parseKey(o.code).includes(filterText)) return true;
    if (o.label && parseKey(o.label).includes(filterText)) return true;
    if (o.definition && parseKey(o.definition).includes(filterText)) return true;

    if (o.codelists) {
      return (o.codelists = o.codelists.filter(f)).length;
    }
    if (o.items) {
      return (o.items = o.items.filter(f)).length;
    }
  });
  return filteredData;
};

const getCategoryById = (categories, categoryId) =>
  categories
    .map(e => {
      return e.id;
    })
    .indexOf(categoryId);

export const CodelistsManagerUtils = {
  filterByText,
  getCategoryById
};
