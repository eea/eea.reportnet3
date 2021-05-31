import dayjs from 'dayjs';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import sortBy from 'lodash/sortBy';

import { apiReferenceDataflow } from 'core/infrastructure/api/domain/model/ReferencedDataflow';

import { CoreUtils, TextUtils } from 'core/infrastructure/CoreUtils';

const all = async userData => {
  const dataflowsDTO = await apiReferenceDataflow.all(userData);

  return dataflowsDTO;
};

export const ApiReferenceDataflowRepository = { all };
