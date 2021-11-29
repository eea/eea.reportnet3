import { atomFamily } from 'recoil';

export const filtersStateFamily = atomFamily({
  key: 'FiltersState',
  default: () => ({
    data: [],
    filterBy: {},
    filteredData: [],
    loadingStatus: 'IDLE'
  })
});
