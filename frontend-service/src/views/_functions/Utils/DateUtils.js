const formatFullDate = (date, isInvalidDate) => {
  if (isInvalidDate) return '';
  let d = new Date(date),
    month = '' + (d.getMonth() + 1),
    day = '' + d.getDate(),
    year = d.getFullYear(),
    hours = ' ' + d.getHours(),
    minutes = d.getMinutes(),
    seconds = d.getSeconds();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;
  if (seconds < 10) seconds = '0' + seconds;

  return [year, month, day].join('-') + [hours, minutes, seconds].join('.');
};

export const DateUtils = {
  formatFullDate
};
