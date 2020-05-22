import isNil from 'lodash/isNil';

import { apiUniqueConstraints } from 'core/infrastructure/api/domain/model/UniqueConstraints/ApiUniqueConstraints';

import { UniqueConstraint } from 'core/domain/model/UniqueConstraints/UniqueConstraint';

const all = async datasetSchemaId => parseConstraintsList(await apiUniqueConstraints.all(datasetSchemaId));

const create = async (datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
  await apiUniqueConstraints.create(datasetSchemaId, fieldSchemaIds, tableSchemaId);

const deleteById = async uniqueConstraintId => {
  return await apiUniqueConstraints.deleteById(uniqueConstraintId);
};

const update = async (datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
  await apiUniqueConstraints.update(datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId);
};

const parseConstraint = constraintDTO => new UniqueConstraint(constraintDTO);

const parseConstraintsList = constraintsDTO => {
  if (!isNil(constraintsDTO)) {
    const constraints = [];
    constraintsDTO.forEach(constraintDTO => constraints.push(parseConstraint(constraintDTO)));
    return constraints;
  }
  return;
};

export const ApiUniqueConstraintsRepository = { all, create, deleteById, update };
