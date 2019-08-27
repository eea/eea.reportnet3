import { LocalUserStorage } from 'core/infrastructure/domain/model/User/LocalUserStorage';

export const UserStorage = {
  get: () => Promise.reject('[UserStorage#get] must be implemented'),
  set: () => Promise.reject('[UserStorage#set] must be implemented'),
  remove: () => Promise.reject('[UserStorage#remove] must be implemented'),
  hasToken: () => Promise.reject('[UserStorage#hasToken] must be implemented')
};

export const userStorage = Object.assign({}, UserStorage, LocalUserStorage);
