import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

const getUserDateTime = timestamp => {
  const dateInCet = dayjs(dayjs(timestamp).tz('CET').format('YYYY-MM-DDTHH:mm'));
  const dateInUtc = dayjs(dayjs(timestamp).tz('UTC').format('YYYY-MM-DDTHH:mm'));

  const cetUtcHourDiff = dateInCet.diff(dateInUtc, 'hours', true);

  let utcDate = dayjs(timestamp).subtract(cetUtcHourDiff, 'hours');
  console.log(cetUtcHourDiff);
  const userTimezone = dayjs.tz.guess();
  return dayjs(utcDate).tz(userTimezone);
};

const convertTimeZoneName = timezone => {
  switch (timezone) {
    case 'Europe/Kyiv':
      return 'Europe/Kiev';
    default:
      return timezone;
  }
};

export const DateTimeUtils = {
  getUserDateTime,
  convertTimeZoneName
};
