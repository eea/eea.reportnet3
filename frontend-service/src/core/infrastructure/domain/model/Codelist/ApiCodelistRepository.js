import { capitalize, isEmpty, isNull, isUndefined } from 'lodash';

import { apiCodelist } from 'core/infrastructure/api/domain/model/Codelist';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const allInCategory = async codelistCategoryId => {
  const codelistsDTO = await apiCodelist.allInCategory(codelistCategoryId);
  const orderedCodelistsDTO = codelistsDTO.data.sort((a, b) => a.id - b.id);
  return orderedCodelistsDTO.map(codelistDTO => {
    const codelistItems = codelistDTO.items.map(
      itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
    );
    return new Codelist(
      codelistDTO.id,
      codelistDTO.name,
      codelistDTO.description,
      codelistDTO.version,
      capitalize(codelistDTO.status.toLowerCase()),
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

const cloneById = async (codelistId, description, items, name, version, categoryId) => {
  const categoryDTO = new CodelistCategory(categoryId);
  const codelistItemsDTO = items.map(item => new CodelistItem(null, item.shortCode, item.label, item.definition, null));
  const codelistDTO = new Codelist(null, name, description, version, undefined, codelistItemsDTO);
  codelistDTO.category = categoryDTO;
  return await apiCodelist.cloneById(codelistId, codelistDTO);
};

const deleteById = async codelistId => {
  return await apiCodelist.deleteById(codelistId);
};

const getById = async codelistId => {
  const codelistDTO = await apiCodelist.getById(codelistId);
  let codelistItems = [];

  if (!isEmpty(codelistDTO.items)) {
    codelistItems = codelistDTO.items.map(
      itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
    );
  }
  return new Codelist(
    codelistDTO.data.id,
    codelistDTO.data.name,
    codelistDTO.data.description,
    codelistDTO.data.version,
    codelistDTO.data.status,
    codelistItems
  );
};

const getCodelistsList = async datasetSchemas => {
  const codelistIds = await getCodelistsIdsBySchemas(datasetSchemas);
  const codelistsList = await getCodelistsByIds(codelistIds);
  return codelistsList;
};

const getCodelistsIdsBySchemas = datasetSchemas => {
  const codelistIds = [];
  try {
    datasetSchemas.forEach(schema => {
      if (!isUndefined(schema)) {
        schema.tables.forEach(table => {
          table.records.forEach(record => {
            record.fields.forEach(field => {
              let codelistId = field.type === 'CODELIST' ? field.codelistId : null;
              if (!isNull(codelistId)) {
                codelistIds.push(codelistId);
              }
            });
          });
        });
      }
    });
  } catch (error) {
    console.log({ error });
  }
  return codelistIds;
};

const getCodelistsByIds = async codelistIds => {
  const codelists = await apiCodelist.getAllByIds(codelistIds);
  return codelists;
};

const updateById = async (id, description, items, name, status, version, categoryId) => {
  const categoryDTO = new CodelistCategory(categoryId);
  console.log({ items });
  const codelistItemsDTO = items.map(
    item => new CodelistItem(item.id.includes('-') ? null : item.id, item.shortCode, item.label, item.definition, id)
  );
  const codelistDTO = new Codelist(id, name, description, version, status, codelistItemsDTO);
  codelistDTO.category = categoryDTO;
  console.log({ codelistDTO });
  return await apiCodelist.updateById(codelistDTO);
};

export const ApiCodelistRepository = {
  addById,
  allInCategory,
  cloneById,
  deleteById,
  getById,
  getCodelistsList,
  updateById
};
