import { LocalUserStorage } from 'core/infrastructure/domain/model/User/LocalUserStorage';

export const UserStorage = {
  get: () => '[UserStorage#get] must be implemented',
  getLocalStorage: () => '[UserStorage#getLocalStorage] must be implemented',
  getPropertyFromLocalStorage: () => '[UserStorage#getPropertyFromLocalStorage] must be implemented',
  hasToken: () => '[UserStorage#hasToken] must be implemented',
  remove: () => '[UserStorage#remove] must be implemented',
  removeLocalProperty: () => '[UserStorage#removeLocalProperty] must be implemented',
  removeLocalStorage: () => '[UserStorage#removeLocalStorage] must be implemented',
  set: () => '[UserStorage#set] must be implemented',
  setLocalStorage: () => '[UserStorage#setLocalStorage] must be implemented',
  setPropertyToLocalStorage: () => '[UserStorage#setPropertyToLocalStorage] must be implemented'
};

export const userStorage = Object.assign({}, UserStorage, LocalUserStorage);
