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
  const codelistItems = codelistDTO.data.items.map(
    itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
  );
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
  if (isEmpty(codelistIds)) {
    return [];
  }
  const codelistsList = await getCodelistsByIds(codelistIds);
  return codelistsList;
};

const getCodelistsIdsBySchemas = datasetSchemas => {
  if (isEmpty(datasetSchemas)) {
    console.log(datasetSchemas);
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
  try {
    const codelistIds = [];
    datasetSchemas.forEach(schema => {
      // if (!isUndefined(schema)) {
      schema.tables.map(table => {
        table.records.map(record => {
          record.fields.map(field => {
            if (!isNull(field.codelistId)) {
              codelistIds.push(field.codelistId);
            }
          });
        });
      });
      // }
    });
    return codelistIds;
  } catch (error) {
    console.log({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
};

const getCodelistsByIds = async codelistIds => {
  if (isEmpty(codelistIds)) {
    return [];
  }
  try {
    const codelistsDTO = await apiCodelist.getAllByIds(codelistIds);
    let codelistItems = [];
    codelistsDTO.data.sort((a, b) => a.id - b.id);
    const codelists = codelistsDTO.data.map(codelistDTO => {
      if (!isEmpty(codelistDTO.items)) {
        codelistItems = codelistDTO.items.map(
          itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
        );
      }
      return new Codelist(
        codelistDTO.id,
        codelistDTO.category.shortCode,
        codelistDTO.category.description,
        codelistDTO.version,
        codelistDTO.status,
        codelistItems
      );
    });
    return codelists;
  } catch (error) {
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_BY_IDS');
  }
};

const getCodelistsListWithSchemas = async datasetSchemas => {
  const codelistIdsWithSchema = await getCodelistsIdsBySchemasWithSchemas(datasetSchemas);
  if (isEmpty(codelistIdsWithSchema)) {
    return [];
  }
  return codelistIdsWithSchema;
};

const getCodelistsIdsBySchema = async datasetSchema => {
  if (isEmpty(datasetSchema)) {
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
  try {
    const codelistIds = [];
    datasetSchema.tables.map(table => {
      table.records.map(record => {
        record.fields.map(field => {
          if (!isNull(field.codelistId)) {
            codelistIds.push(field.codelistId);
          }
        });
      });
    });
    // }
    return codelistIds;
  } catch (error) {
    console.log({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
};

const getCodelistsIdsBySchemasWithSchemas = async datasetSchemas => {
  if (isEmpty(datasetSchemas)) {
    console.log(datasetSchemas);
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
  try {
    const codelistList = datasetSchemas.map(async schema => {
      const codelist = {};
      let ids = await getCodelistsIdsBySchema(schema);
      codelist.codelists = await getCodelistsByCodelistsIds(ids);
      codelist.schema = schema;
      return codelist;
    });
    return Promise.all(codelistList).then(codelistsListWithSchemas => codelistsListWithSchemas);
  } catch (error) {
    console.log({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
};

const getCodelistsByCodelistsIds = async codelistIds => {
  if (isEmpty(codelistIds)) {
    return [];
  }
  try {
    const codelistsDTO = await apiCodelist.getAllByIds(codelistIds);
    let codelistItems = [];
    codelistsDTO.data.sort((a, b) => a.id - b.id);
    const codelists = codelistsDTO.data.map(codelistDTO => {
      if (!isEmpty(codelistDTO.items)) {
        codelistItems = codelistDTO.items.map(
          itemDTO => new CodelistItem(itemDTO.id, itemDTO.shortCode, itemDTO.label, itemDTO.definition, codelistDTO.id)
        );
      }
      return new Codelist(
        codelistDTO.id,
        codelistDTO.name,
        codelistDTO.description,
        codelistDTO.version,
        codelistDTO.status,
        codelistItems
      );
    });
    return codelists;
  } catch (error) {
    console.log({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_BY_IDS');
  }
};

const updateById = async (id, description, items, name, status, version, categoryId) => {
  const categoryDTO = new CodelistCategory(categoryId);
  const codelistItemsDTO = items.map(
    item =>
      new CodelistItem(
        item.id.toString().includes('-') ? null : item.id,
        item.shortCode,
        item.label,
        item.definition,
        id
      )
  );
  const codelistDTO = new Codelist(id, name, description, version, status, codelistItemsDTO);
  codelistDTO.category = categoryDTO;
  return await apiCodelist.updateById(codelistDTO);
};

export const ApiCodelistRepository = {
  addById,
  allInCategory,
  cloneById,
  deleteById,
  getById,
  getCodelistsList,
  getCodelistsListWithSchemas,
  updateById
};
