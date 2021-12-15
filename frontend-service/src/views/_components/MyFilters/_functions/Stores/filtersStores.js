import { atomFamily } from 'recoil';

export const filtersStateFamily = atomFamily({
  key: 'filtersState',
  default: () => ({ filterBy: {}, filteredData: [] })
});

export const filterByKeysFamily = atomFamily({
  key: 'filterByKeys',
  default: () => ({ CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [] })
});

export const sortByStateFamily = atomFamily({
  key: 'sortByStateFamily',
  default: () => ({})
});
