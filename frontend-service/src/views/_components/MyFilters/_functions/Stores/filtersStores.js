import { atomFamily } from 'recoil';

export const filtersStateFamily = atomFamily({
  key: 'filtersState',
  default: () => ({ filterBy: {}, filteredData: [] })
});

export const filteredDataState = atomFamily({
  key: 'filteredData',
  default: () => []
});

export const filterByState = atomFamily({
  key: 'filterBy',
  default: () => ({})
});

export const filterByKeysFamily = atomFamily({
  key: 'filterByKeys',
  default: () => ({ CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [], SEARCH: [] })
});

export const sortByStateFamily = atomFamily({
  key: 'sortByStateFamily',
  default: () => ({})
});

export const searchStateFamily = atomFamily({
  key: 'searchStateFamily',
  default: () => ''
});
