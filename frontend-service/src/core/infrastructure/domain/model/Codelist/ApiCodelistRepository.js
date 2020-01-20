import { isEmpty, isNull } from 'lodash';

import { apiCodelist } from 'core/infrastructure/api/domain/model/Codelist';
import { CodelistCategory } from 'core/domain/model/CodelistCategory/CodelistCategory';
import { Codelist } from 'core/domain/model/Codelist/Codelist';
import { CodelistItem } from 'core/domain/model/Codelist/CodelistItem/CodelistItem';

const allInCategory = async codelistCategoryId => {
  const codelistsDTO = await apiCodelist.allInCategory(codelistCategoryId);

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
  const codelistDTO = new Codelist(null, name, description, Number(version), status, codelistItemsDTO);
  codelistDTO.category = categoryDTO;
  return await apiCodelist.addById(codelistDTO);
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
  datasetSchemas.forEach(schema => {
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
  });
  return codelistIds;
};

const getCodelistsByIds = async codelistIds => {
  const codelists = await apiCodelist.getAllByIds(codelistIds);
  return codelists;
};

const updateById = async (id, description, items, name, status, version) => {
  const codelistItemsDTO = items.map(
    item => new CodelistItem(item.codelistItemId, item.shortCode, item.label, item.definition, id)
  );
  const codelistDTO = new Codelist(id, name, description, version, status, codelistItemsDTO);
  return await apiCodelist.updateById(codelistDTO);
};

export const ApiCodelistRepository = {
  addById,
  allInCategory,
  deleteById,
  getById,
  getCodelistsList,
  updateById
};
