import { useContext } from 'react';

import dayjs from 'dayjs';

import { UserContext } from 'views/_functions/Contexts/UserContext';

export const useDateTimeFormatByUserPreferences = () => {
  const userContext = useContext(UserContext);

  const getDateTimeFormatByUserPreferences = timestamp => {
    return dayjs(timestamp).format(
      `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
        userContext.userProps.amPm24h ? '' : ' A'
      }`
    );
  };
  return { getDateTimeFormatByUserPreferences };
};
