import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const parseSortField = sortField => {
  if (isNil(sortField) || isEmpty(sortField)) {
    return undefined;
  }

  const replacements = {
    processStartingDate: 'date_start',
    processFinishingDate: 'date_finish',
    queuedDate: 'queued_date',
    dataset: 'dataset_name',
    dataflow: 'dataflow_name'
  };

  return replacements[sortField] || sortField;
};

export const BackgroundProcessUtils = {
  parseSortField
};
