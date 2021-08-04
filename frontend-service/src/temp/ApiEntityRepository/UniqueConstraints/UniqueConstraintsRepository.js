import { ApiUniqueConstraintsRepository } from 'repositories/_temp/model/UniqueConstraints/ApiUniqueConstraintsRepository';

export const UniqueConstraintsRepository = {
  all: () => Promise.reject('[UniqueConstraintsRepository#all] must be implemented'),
  create: () => Promise.reject('[UniqueConstraintsRepository#create] must be implemented'),
  deleteById: () => Promise.reject('[UniqueConstraintsRepository#deleteById] must be implemented'),
  update: () => Promise.reject('[UniqueConstraintsRepository#update] must be implemented')
};

export const uniqueConstraintsRepository = Object.assign(
  {},
  UniqueConstraintsRepository,
  ApiUniqueConstraintsRepository
);
