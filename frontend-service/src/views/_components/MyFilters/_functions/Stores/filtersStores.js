import { atomFamily } from 'recoil';

export const filterByKeysState = atomFamily({
  key: 'filterByKeysState',
  default: () => ({ CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [], SEARCH: [] })
});

export const filterByState = atomFamily({
  key: 'filterByState',
  default: () => ({})
});

export const filteredDataState = atomFamily({
  key: 'filteredDataState',
  default: () => []
});

export const searchState = atomFamily({
  key: 'searchState',
  default: () => ''
});

export const sortByState = atomFamily({
  key: 'sortByState',
  default: () => ({})
});
