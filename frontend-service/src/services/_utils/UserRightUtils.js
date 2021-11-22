import { UserRight } from 'entities/UserRight';

import sortBy from 'lodash/sortBy';
import uniqueId from 'lodash/uniqueId';

const parseUserRightListDTO = userRightListDTO => {
  const userRightList = userRightListDTO?.map(userRightDTO => {
    userRightDTO.id = uniqueId();

    return new UserRight({
      account: userRightDTO.account,
      id: userRightDTO.id,
      isNew: userRightDTO.isNew,
      isValid: !userRightDTO.invalid,
      role: userRightDTO.role
    });
  });

  return sortBy(userRightList, ['account']);
};

export const UserRightUtils = {
  parseUserRightListDTO
};
