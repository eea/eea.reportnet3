import { atomFamily } from 'recoil';

export const filtersStateFamily = atomFamily({
  key: 'FiltersState',
  default: () => ({
    data: [],
    filterBy: {},
    filterByTypes: {},
    filteredData: [],
    loadingStatus: 'IDLE'
  })
});

export const filterByKeysFamily = atomFamily({
  key: 'FilterByKeys',
  default: () => ({ CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [] })
});

export const sortByStateFamily = atomFamily({
  key: 'SortByStateFamily',
  default: () => ({})
});
