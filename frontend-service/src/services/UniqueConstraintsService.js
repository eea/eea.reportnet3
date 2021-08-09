import sortBy from 'lodash/sortBy';

import { UniqueConstraintsRepository } from 'repositories/UniqueConstraintsRepository';

import { UniqueConstraint } from 'entities/UniqueConstraint';

const parseConstraintsList = (constraintsDTO = []) => {
  const constraints = constraintsDTO.map(constraintDTO => new UniqueConstraint(constraintDTO));
  return sortBy(constraints, ['uniqueId']);
};

export const UniqueConstraintsService = {
  getAll: async (dataflowId, datasetSchemaId) => {
    const response = await UniqueConstraintsRepository.getAll(dataflowId, datasetSchemaId);
    return parseConstraintsList(response.data);
  },

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
    await UniqueConstraintsRepository.create(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId),

  delete: async (dataflowId, uniqueConstraintId) =>
    await UniqueConstraintsRepository.delete(dataflowId, uniqueConstraintId),

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) =>
    await UniqueConstraintsRepository.update(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId)
};
