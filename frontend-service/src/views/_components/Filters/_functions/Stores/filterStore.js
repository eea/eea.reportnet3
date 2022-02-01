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
    key: 'studentSearch',
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
