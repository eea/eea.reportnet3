import { apiCodelistCategory } from 'core/infrastructure/api/domain/model/CodelistCategory';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const all = async () => {
  const categoriesDTO = await apiCodelistCategory.all();

  return categoriesDTO.map(categorieDTO => {
    const codelists = categorieDTO.codelists.map(codelistDTO => {
      const codelistItems = codelistDTO.items.map(
        itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
      );
      return new Codelist(
        codelistDTO.id,
        codelistDTO.name,
        codelistDTO.description,
        codelistDTO.version,
        codelistDTO.status,
        codelistItems
      );
    });
    return new CodelistCategory(categorieDTO.id, categorieDTO.shortCode, categorieDTO.description, codelists);
  });
};

const addById = async (shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory(null, shortCode, description);
  return await apiCodelistCategory.addById(codelistCategoryDTO);
};

const deleteById = async categoryId => {
  return await apiCodelistCategory.deleteById(categoryId);
};

const updateById = async (id, shortCode, description) => {
  const codelistCategoryDTO = new CodelistCategory(id, shortCode, description);
  return await apiCodelistCategory.updateById(codelistCategoryDTO);
};

export const ApiCodelistCategoryRepository = {
  all,
  addById,
  deleteById,
  updateById
};
