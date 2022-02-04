import { atomFamily } from 'recoil';

export const filterByAllKeys = atomFamily({
  key: 'filterByAllKeys',
  default: []
});

export const filterByKeyCheckboxStore = atomFamily({
  key: 'filterByKeyCheckboxStore',
  default: { keys: [], nestedKey: null }
});

export const filterByKeyDateStore = atomFamily({
  key: 'filterByKeyDateStore',
  default: { keys: [], nestedKey: null }
});

export const filterByKeyDropdownStore = atomFamily({
  key: 'filterByKeyDropdownStore',
  default: { keys: [], nestedKey: null }
});

export const filterByKeyInputStore = atomFamily({
  key: 'filterByKeyInputStore',
  default: { keys: [], nestedKey: null }
});

export const filterByKeyMultiSelectStore = atomFamily({
  key: 'filterByKeyMultiSelectStore',
  default: { keys: [], nestedKey: null }
});

export const filterByKeySearchStore = atomFamily({
  key: 'filterByKeySearchStore',
  default: { keys: [], nestedKey: null }
});
