import { isUndefined } from 'lodash';

import { apiCodelistCategory } from 'core/infrastructure/api/domain/model/CodelistCategory';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const addById = async (shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory(null, shortCode, description);
  return await apiCodelistCategory.addById(codelistCategoryDTO);
};

const all = async () => {
  const categoriesDTO = await apiCodelistCategory.all();
  const orderedCategoriesDTO = categoriesDTO.data.sort((a, b) => a.id - b.id);
  return orderedCategoriesDTO.map(categoryDTO => {
    const codelists = !isUndefined(categoryDTO.codelists)
      ? categoryDTO.codelists.map(codelistDTO => {
          const codelistItems = !isUndefined(codelistDTO.items)
            ? codelistDTO.items.map(
                itemDTO =>
                  new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
              )
            : [];
          return new Codelist(
            codelistDTO.id,
            codelistDTO.shortCode,
            codelistDTO.description,
            codelistDTO.version,
            codelistDTO.status,
            codelistItems
          );
        })
      : [];
    return new CodelistCategory(categoryDTO.id, categoryDTO.shortCode, categoryDTO.description, codelists);
  });
};

const deleteById = async codelistCategoryId => {
  console.log({ codelistCategoryId });
  return await apiCodelistCategory.deleteById(codelistCategoryId);
};

const getCategoryInfo = async codelistCategoryId => {
  const categoryDTO = await apiCodelistCategory.getCategoryInfo(codelistCategoryId);
  return new CodelistCategory(categoryDTO.id, categoryDTO.shortCode, categoryDTO.description, null);
};

const updateById = async (id, shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory(id, shortCode, description);
  return await apiCodelistCategory.updateById(codelistCategoryDTO);
};

export const ApiCodelistCategoryRepository = {
  all,
  addById,
  deleteById,
  getCategoryInfo,
  updateById
};
