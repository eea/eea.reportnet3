import { isUndefined } from 'lodash';

import { apiCodelistCategory } from 'core/infrastructure/api/domain/model/CodelistCategory';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const addById = async (shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory({ description, shortCode });
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
                  new CodelistItem({
                    codelistId: codelistDTO.id,
                    definition: itemDTO.definition,
                    id: itemDTO.id,
                    label: itemDTO.label,
                    shortCode: itemDTO.shortCode
                  })
              )
            : [];
          return new Codelist({
            description: codelistDTO.description,
            id: codelistDTO.id,
            items: codelistItems,
            name: codelistDTO.name,
            status: codelistDTO.status,
            version: codelistDTO.version
          });
        })
      : [];
    return new CodelistCategory({
      codelistNumber: categoryDTO.codelistNumber,
      codelists,
      id: categoryDTO.id,
      description: categoryDTO.description,
      shortCode: categoryDTO.shortCode
    });
  });
};

const deleteById = async codelistCategoryId => await apiCodelistCategory.deleteById(codelistCategoryId);

const getCategoryInfo = async codelistCategoryId => {
  const categoryDTO = await apiCodelistCategory.getCategoryInfo(codelistCategoryId);
  return new CodelistCategory({
    description: categoryDTO.description,
    id: categoryDTO.id,
    shortCode: categoryDTO.shortCode
  });
};

const updateById = async (id, shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory({ description, id, shortCode });
  return await apiCodelistCategory.updateById(codelistCategoryDTO);
};

export const ApiCodelistCategoryRepository = {
  all,
  addById,
  deleteById,
  getCategoryInfo,
  updateById
};
