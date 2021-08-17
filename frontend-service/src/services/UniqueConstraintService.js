import { UniqueConstraintRepository } from 'repositories/UniqueConstraintRepository';

import { UniqueConstraintUtils } from 'services/_utils/UniqueConstraintUtils';

export const UniqueConstraintService = {
  getAll: async (dataflowId, datasetSchemaId) => {
    const response = await UniqueConstraintRepository.getAll(dataflowId, datasetSchemaId);
    return UniqueConstraintUtils.parseConstraintsList(response.data);
  },

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
    await UniqueConstraintRepository.create(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId),

  delete: async (dataflowId, uniqueConstraintId) =>
    await UniqueConstraintRepository.delete(dataflowId, uniqueConstraintId),

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) =>
    await UniqueConstraintRepository.update(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId)
};
