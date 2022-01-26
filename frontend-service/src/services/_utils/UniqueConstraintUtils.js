import sortBy from 'lodash/sortBy';

import { UniqueConstraint } from 'entities/UniqueConstraint';

const parseConstraintsList = uniqueConstraintsDTO => {
  const constraints = uniqueConstraintsDTO?.map(constraintDTO => new UniqueConstraint(constraintDTO));
  return sortBy(constraints, ['uniqueId']);
};

export const UniqueConstraintUtils = {
  parseConstraintsList
};
