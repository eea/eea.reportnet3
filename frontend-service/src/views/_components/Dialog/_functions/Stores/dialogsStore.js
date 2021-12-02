import { atom } from 'recoil';

export const dialogsStore = atom({
  key: 'openedDialogs',
  default: []
});
