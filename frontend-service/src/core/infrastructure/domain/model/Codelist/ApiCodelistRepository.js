import { apiCodelist } from 'core/infrastructure/api/domain/model/Codelist';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const all = async () => {
  const codelistsDTO = await apiCodelist.all();

  return codelistsDTO.map(codelistDTO => {
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
};

const addById = async (description, items, name, status, version, categoryId) => {
  const categoryDTO = new CodelistCategory(categoryId);
  const codelistItemsDTO = items.map(item => new CodelistItem(null, item.shortCode, item.label, item.definition, null));
  const codelistDTO = new Codelist(null, name, description, version, status, codelistItemsDTO);
  codelistDTO.category = categoryDTO;
  return await apiCodelist.addById(codelistDTO);
};

const deleteById = async codelistId => {
  return await apiCodelist.deleteById(codelistId);
};

const updateById = async (id, description, items, name, status, version) => {
  const codelistItemsDTO = items.map(
    item => new CodelistItem(item.codelistItemId, item.shortCode, item.label, item.definition, id)
  );
  const codelistDTO = new Codelist(id, name, description, version, status, codelistItemsDTO);
  return await apiCodelist.updateById(codelistDTO);
};

export const ApiCodelistRepository = {
  all,
  addById,
  deleteById,
  updateById
};
