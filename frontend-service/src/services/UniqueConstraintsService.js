import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { UniqueConstraintsRepository } from 'repositories/UniqueConstraintsRepository';

import { UniqueConstraint } from 'entities/UniqueConstraint';

const all = async (dataflowId, datasetSchemaId) => {
  const uniqueConstraints = await UniqueConstraintsRepository.all(dataflowId, datasetSchemaId);
  uniqueConstraints.data = parseConstraintsList(uniqueConstraints.data);
  return uniqueConstraints;
};

const create = async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
  await UniqueConstraintsRepository.create(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId);

const deleteById = async (dataflowId, uniqueConstraintId) => {
  return await UniqueConstraintsRepository.deleteById(dataflowId, uniqueConstraintId);
};

const update = async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
  return await UniqueConstraintsRepository.update(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
};

const parseConstraint = constraintDTO => new UniqueConstraint(constraintDTO);

const parseConstraintsList = constraintsDTO => {
  if (!isNil(constraintsDTO)) {
    const constraints = [];
    constraintsDTO.forEach(constraintDTO => constraints.push(parseConstraint(constraintDTO)));
    return sortBy(constraints, ['uniqueId']);
  }
  return;
};

export const UniqueConstraintsService = { all, create, deleteById, update };
