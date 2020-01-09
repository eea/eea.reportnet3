import { ApiCodelistRepository } from 'core/infrastructure/domain/model/Codelist/ApiCodelistRepository';

export const CodelistRepository = {
  all: () => Promise.reject('[CodelistRepository#all] must be implemented'),
  addById: () => Promise.reject('[CodelistRepository#addById] must be implemented'),
  deleteById: () => Promise.reject('[CodelistRepository#deleteById] must be implemented'),
  updateById: () => Promise.reject('[CodelistRepository#updateById] must be implemented')
};

export const codelistRepository = Object.assign({}, CodelistRepository, ApiCodelistRepository);
