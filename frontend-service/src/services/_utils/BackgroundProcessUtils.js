import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { BackgroundProcess } from 'entities/BackgroundProcess';

const parseSortField = sortField => {
  if (isNil(sortField) || isEmpty(sortField)) {
    return undefined;
  }

  const replacements = {
    dataflow: 'name',
    dataset: 'dataset_name',
    processFinishingDate: 'date_finish',
    processStartingDate: 'date_start',
    queuedDate: 'queued_date',
    user: 'username'
  };

  return replacements[sortField] || sortField;
};

const parseValidationsStatusListDTO = validationsStatuses =>
  validationsStatuses?.map(validationsStatus => parseValidationsStatusDTO(validationsStatus));

const parseValidationsStatusDTO = validationsStatus => new BackgroundProcess(validationsStatus);

export const BackgroundProcessUtils = {
  parseSortField,
  parseValidationsStatusListDTO
};
