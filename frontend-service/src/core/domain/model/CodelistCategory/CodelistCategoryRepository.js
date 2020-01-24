import { ApiCodelistCategoryRepository } from 'core/infrastructure/domain/model/CodelistCategory/ApiCodelistCategoryRepository';

export const CodelistCategoryRepository = {
  all: () => Promise.reject('[CodelistCategoryRepository#all] must be implemented'),
  addById: () => Promise.reject('[CodelistCategoryRepository#addById] must be implemented'),
  deleteById: () => Promise.reject('[CodelistCategoryRepository#deleteById] must be implemented'),
  getCategoryInfo: () => Promise.reject('[CodelistRepository#getCategoryInfo] must be implemented'),
  updateById: () => Promise.reject('[CodelistCategoryRepository#updateById] must be implemented')
};

export const codelistCategoryRepository = Object.assign({}, CodelistCategoryRepository, ApiCodelistCategoryRepository);
