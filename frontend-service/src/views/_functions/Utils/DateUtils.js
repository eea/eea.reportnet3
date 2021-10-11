const formatDate = (date, isInvalidDate, fullDate) => {
  if (isInvalidDate) return '';
  let d = new Date(date),
    month = '' + (d.getMonth() + 1),
    day = '' + d.getDate(),
    year = d.getFullYear(),
    hours = '' + d.getHours(),
    minutes = d.getMinutes(),
    seconds = d.getSeconds();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  if (fullDate) {
    return `${[year, month, day].join('-')} ${[date.getHours(), date.getMinutes(), date.getSeconds()].join('.')}`;
  }

  return [year, month, day].join('-')[(hours, minutes, seconds)].join('-');
};

export const DateUtils = {
  formatDate
};
