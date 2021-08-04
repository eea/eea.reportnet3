import { LocalUserStorage } from 'repositories/_temp/model/User/LocalUserStorage';

export const UserStorage = {
  getTokens: () => '[UserStorage#getTokens] must be implemented',
  getSessionStorage: () => '[UserStorage#getSessionStorage] must be implemented',
  getPropertyFromSessionStorage: () => '[UserStorage#getPropertyFromLocalStorage] must be implemented',
  hasToken: () => '[UserStorage#hasToken] must be implemented',
  remove: () => '[UserStorage#remove] must be implemented',
  removeSessionStorageProperty: () => '[UserStorage#removeLocalProperty] must be implemented',
  removeSessionStorage: () => '[UserStorage#removeLocalStorage] must be implemented',
  set: () => '[UserStorage#set] must be implemented',
  setSessionStorage: () => '[UserStorage#setLocalStorage] must be implemented',
  setPropertyToSessionStorage: () => '[UserStorage#setPropertyToLocalStorage] must be implemented'
};

export const userStorage = Object.assign({}, UserStorage, LocalUserStorage);
