import { ApiUniqueConstraintsRepository } from 'core/infrastructure/domain/model/UniqueConstraints/ApiUniqueConstraintsRepository';

export const UniqueConstraintsRepository = {
  all: () => Promise.reject('[UniqueConstraintsRepository#all] must be implemented')
};

export const uniqueConstraintsRepository = Object.assign(
  {},
  UniqueConstraintsRepository,
  ApiUniqueConstraintsRepository
);
