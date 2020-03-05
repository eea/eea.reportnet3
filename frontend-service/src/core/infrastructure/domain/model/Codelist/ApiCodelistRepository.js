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
      itemDTO =>
        new CodelistItem({
          codelistId: codelistDTO.id,
          definition: itemDTO.definition,
          id: itemDTO.id,
          label: itemDTO.label,
          shortCode: itemDTO.shortCode
        })
    );
    return new Codelist({
      id: codelistDTO.id,
      name: codelistDTO.name,
      description: codelistDTO.description,
      version: codelistDTO.version,
      status: capitalize(codelistDTO.status.toLowerCase()),
      items: codelistItems
    });
  });
};

const addById = async (description, items, name, status, version, categoryId) => {
  const categoryDTO = new CodelistCategory({ id: categoryId });
  const codelistItemsDTO = items.map(item => new CodelistItem(item));
  const codelistDTO = new Codelist({ description, items: codelistItemsDTO, name, status, version });
  codelistDTO.category = categoryDTO;
  return await apiCodelist.addById(codelistDTO);
};

const cloneById = async (codelistId, description, items, name, version, categoryId) => {
  const categoryDTO = new CodelistCategory({ id: categoryId });
  const codelistItemsDTO = items.map(item => new CodelistItem(item));
  const codelistDTO = new Codelist({ description, items: codelistItemsDTO, name, version });
  codelistDTO.category = categoryDTO;
  return await apiCodelist.cloneById(codelistId, codelistDTO);
};

const deleteById = async codelistId => {
  return await apiCodelist.deleteById(codelistId);
};

const getById = async codelistId => {
  const codelistDTO = await apiCodelist.getById(codelistId);
  const codelistItems = codelistDTO.data.items.map(
    itemDTO =>
      new CodelistItem({
        codelistId: codelistDTO.id,
        definition: itemDTO.definition,
        id: itemDTO.id,
        label: itemDTO.label,
        shortCode: itemDTO.shortCode
      })
  );
  return new Codelist({
    description: codelistDTO.data.description,
    id: codelistDTO.data.id,
    items: codelistItems,
    name: codelistDTO.data.name,
    status: codelistDTO.data.status,
    version: codelistDTO.data.version
  });
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
    console.error({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
};

const getCodelistsByIds = async codelistIds => {
  if (isEmpty(codelistIds)) {
    return [];
  }
  try {
    const codelistsDTO = await apiCodelist.getAllByIds(codelistIds);
    console.log({ codelistsDTO });
    let codelistItems = [];
    codelistsDTO.data.sort((a, b) => a.id - b.id);
    const codelists = codelistsDTO.data.map(codelistDTO => {
      if (!isEmpty(codelistDTO.items)) {
        codelistItems = codelistDTO.items.map(
          itemDTO =>
            new CodelistItem({
              codelistId: codelistDTO.id,
              definition: itemDTO.definition,
              id: itemDTO.id,
              label: itemDTO.label,
              shortCode: itemDTO.shortCode
            })
        );
      }
      console.log({ codelistDTO });
      return new Codelist({
        description: codelistDTO.description,
        id: codelistDTO.id,
        items: codelistItems,
        name: codelistDTO.name,
        status: codelistDTO.status,
        version: codelistDTO.version
      });
    });
    console.log({ codelists });
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
    console.error({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_IDS_BY_SCHEMAS');
  }
};

const getCodelistsIdsBySchemasWithSchemas = async datasetSchemas => {
  if (isEmpty(datasetSchemas)) {
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
    console.error({ error });
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
        console.log({ codelistDTO });
        codelistItems = codelistDTO.items.map(
          itemDTO =>
            new CodelistItem({
              codelistId: codelistDTO.id,
              definition: itemDTO.definition,
              id: itemDTO.id,
              label: itemDTO.label,
              shortCode: itemDTO.shortCode
            })
        );
      }
      return new Codelist({
        id: codelistDTO.id,
        name: codelistDTO.name,
        description: codelistDTO.description,
        version: codelistDTO.version,
        status: codelistDTO.status,
        items: codelistItems
      });
    });
    return codelists;
  } catch (error) {
    console.error({ error });
    throw new Error('CODELIST_SERVICE_GET_CODELISTS_BY_IDS');
  }
};

const updateById = async (id, description, items, name, status, version, categoryId) => {
  const categoryDTO = new CodelistCategory({ id: categoryId });
  const codelistItemsDTO = items.map(
    item =>
      new CodelistItem({
        codelistId: id,
        definition: item.definition,
        id: item.id.toString().includes('-') ? null : item.id,
        label: item.label,
        shortCode: item.shortCode
      })
  );
  const codelistDTO = new Codelist({ description, id, items: codelistItemsDTO, name, status, version });
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
