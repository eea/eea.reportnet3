import { apiCodelist } from 'core/infrastructure/api/domain/model/Codelist';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const all = async dataflowId => {
  const codelistsDTO = await apiCodelist.all(dataflowId);

  return codelistsDTO.map(codelistDTO => {
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
};

const addById = async (dataflowId, description, items, name, status, version) => {
  return await apiCodelist.addById(dataflowId, description, items, name, status, version);
};

const deleteById = async (dataflowId, codelistId) => {
  return await apiCodelist.deleteById(dataflowId, codelistId);
};

const updateById = async (dataflowId, codelistId, codelist) => {
  return await apiCodelist.updateById(dataflowId, codelistId, codelist);
};

export const ApiCodelistRepository = {
  all,
  addById,
  deleteById,
  updateById
};
