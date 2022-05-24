import { atom, atomFamily, selectorFamily } from 'recoil';

export const dataStore = atomFamily({
  key: 'dataStore',
  default: []
});

export const filterByStore = atomFamily({
  key: 'filterByStore',
  default: {}
});

export const searchByStore = atomFamily({
  key: 'searchByStore',
  default: ''
});

export const sortByStore = atomFamily({
  key: 'sortByStore',
  default: { sortByHeader: '', sortByOption: 'idle' }
});

export const filteredDataStore = atomFamily({
  key: 'filteredDataStore',
  default: selectorFamily({
    key: 'filteredDataStore',
    get:
      id =>
      ({ get }) =>
        get(dataStore(id))
  })
});

export const isStrictModeStore = atom({
  key: 'isStrictModeStore',
  default: false
});

export const isFilteredStore = atomFamily({
  key: 'isFilteredStore',
  default: () => false
});

export const filterByCustomFilterStore = atomFamily({
  key: 'filterByCustomFilterStore',
  default: {}
});
