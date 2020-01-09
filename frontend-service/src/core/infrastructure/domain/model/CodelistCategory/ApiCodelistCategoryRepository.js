import { apiCodelistCategory } from 'core/infrastructure/api/domain/model/CodelistCategory';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const all = async dataflowId => {
  const categoriesDTO = await apiCodelistCategory.all(dataflowId);

  return categoriesDTO.map(categorieDTO => {
    const codelists = categorieDTO.codelists.map(codelistDTO => {
      const codelistItems = codelistDTO.items.map(
        itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition)
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

const addById = async (dataflowId, name, description, codelists) => {
  return await apiCodelistCategory.addById(dataflowId, name, description, codelists);
};

const deleteById = async (dataflowId, categoryId) => {
  return await apiCodelistCategory.deleteById(dataflowId, categoryId);
};

const updateById = async (dataflowId, categoryId, category) => {
  return await apiCodelistCategory.updateById(dataflowId, categoryId, category);
};

export const ApiCodelistCategoryRepository = {
  all,
  addById,
  deleteById,
  updateById
};
