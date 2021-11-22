import { UserRight } from 'entities/UserRight';

import sortBy from 'lodash/sortBy';
import uniqueId from 'lodash/uniqueId';

const parseUserRightListDTO = userRightListDTO => {
  const userRightList = userRightListDTO?.map(userRightDTO => {
    userRightDTO.id = uniqueId();
    return new UserRight({ ...userRightDTO, isValid: !userRightDTO.invalid });
  });

  return sortBy(userRightList, ['account']);
};

export const UserRightUtils = {
  parseUserRightListDTO
};
