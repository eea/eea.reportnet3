import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';

import { apiUniqueConstraints } from 'core/infrastructure/api/domain/model/UniqueConstraints/ApiUniqueConstraints';

import { UniqueConstraint } from 'core/domain/model/UniqueConstraints/UniqueConstraint';

const all = async (dataflowId, datasetSchemaId) =>
  parseConstraintsList(await apiUniqueConstraints.all(dataflowId, datasetSchemaId));

const create = async (datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
  await apiUniqueConstraints.create(datasetSchemaId, fieldSchemaIds, tableSchemaId);

const deleteById = async (uniqueConstraintId, dataflowId) => {
  return await apiUniqueConstraints.deleteById(uniqueConstraintId, dataflowId);
};

const update = async (datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
  return await apiUniqueConstraints.update(datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
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
