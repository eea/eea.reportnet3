import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { apiUniqueConstraints } from 'core/infrastructure/api/domain/model/UniqueConstraints/ApiUniqueConstraints';

import { UniqueConstraint } from 'core/domain/model/UniqueConstraints/UniqueConstraint';

const all = async (dataflowId, datasetSchemaId) => {
  const uniqueConstraints = await apiUniqueConstraints.all(dataflowId, datasetSchemaId);
  uniqueConstraints.data = parseConstraintsList(uniqueConstraints.data);
  return uniqueConstraints;
};

const create = async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
  await apiUniqueConstraints.create(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId);

const deleteById = async (dataflowId, uniqueConstraintId) => {
  return await apiUniqueConstraints.deleteById(dataflowId, uniqueConstraintId);
};

const update = async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
  return await apiUniqueConstraints.update(dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
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

export const ApiUniqueConstraintsRepository = { all, create, deleteById, update };
