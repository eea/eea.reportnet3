import { ApiCodelistRepository } from 'core/infrastructure/domain/model/Codelist/ApiCodelistRepository';

export const CodelistRepository = {
  addById: () => Promise.reject('[CodelistRepository#addById] must be implemented'),
  allInCategory: () => Promise.reject('[CodelistRepository#allInCategory] must be implemented'),
  deleteById: () => Promise.reject('[CodelistRepository#deleteById] must be implemented'),
  getById: () => Promise.reject('[CodelistRepository#getById] must be implemented'),
  updateById: () => Promise.reject('[CodelistRepository#updateById] must be implemented')
};

export const codelistRepository = Object.assign({}, CodelistRepository, ApiCodelistRepository);
