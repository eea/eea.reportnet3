import { atom } from 'recoil';

export const geoJSONListStore = atom({
  key: 'selectedLine',
  default: -1
});
