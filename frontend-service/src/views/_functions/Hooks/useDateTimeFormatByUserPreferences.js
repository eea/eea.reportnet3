import { useContext } from 'react';

import dayjs from 'dayjs';

import { UserContext } from 'views/_functions/Contexts/UserContext';

export const useDateTimeFormatByUserPreferences = () => {
  const userContext = useContext(UserContext);

  const getDateTimeFormatByUserPreferences = (timestamp, hasCET = false) =>
    dayjs(timestamp).format(
      `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
        userContext.userProps.amPm24h ? '' : ' A'
      } ${hasCET ? 'CET' : ''}`
    );

  const getDateDifferenceInMinutes = timestamp => {
    const date1 = dayjs(timestamp);
    const date2 = dayjs();
    const dateDifference = date2.diff(date1, 'minute');

    return dateDifference;
  };

  return { getDateTimeFormatByUserPreferences, getDateDifferenceInMinutes };
};
