import isNil from 'lodash/isNil';

import { apiUniqueConstraints } from 'core/infrastructure/api/domain/model/UniqueConstraints/ApiUniqueConstraints';

import { DatasetTableField } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableField/DatasetTableField';

const all = async datasetSchemaId => parseConstraintsList(await apiUniqueConstraints.all(datasetSchemaId));

const create = async (description, fieldSchemaId, name) =>
  await apiUniqueConstraints.create(description, fieldSchemaId, name);

const deleteById = async (datasetSchemaId, fieldId) => {
  return await apiUniqueConstraints.deleteById(datasetSchemaId, fieldId);
};

const update = async (description, fieldId, fieldSchemaId, name) => {
  await apiUniqueConstraints.update(description, fieldId, fieldSchemaId, name);
};

const parseConstraint = constraintDTO =>
  new DatasetTableField({
    codelistItems: constraintDTO.codelistItems,
    description: constraintDTO.description,
    fieldId: constraintDTO.id,
    name: constraintDTO.name,
    pk: !isNil(constraintDTO.pk) ? constraintDTO.pk : false,
    pkMustBeUsed: !isNil(constraintDTO.pkMustBeUsed) ? constraintDTO.pkMustBeUsed : false,
    pkReferenced: !isNil(constraintDTO.pkReferenced) ? constraintDTO.pkReferenced : false,
    recordId: constraintDTO.idRecord,
    referencedField: constraintDTO.referencedField,
    required: constraintDTO.required,
    type: constraintDTO.type,
    unique: constraintDTO.unique
  });

const parseConstraintsList = constraintsDTO => {
  if (!isNil(constraintsDTO)) {
    const constraints = [];
    constraintsDTO.forEach(constraintDTO => constraints.push(parseConstraint(constraintDTO)));
    return constraints;
  }
  return;
};

export const ApiUniqueConstraintsRepository = { all, create, deleteById, update };
