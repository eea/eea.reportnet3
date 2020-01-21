import React from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';

export const Calendar = ({ className, dateFormat, key, monthNavigator, onChange, value, yearNavigator, yearRange }) => {
  return (
    <PrimeCalendar
      className={className}
      dateFormat={dateFormat}
      key={key}
      monthNavigator={monthNavigator}
      onChange={onChange}
      value={value}
      yearNavigator={yearNavigator}
      yearRange={yearRange}
    />
  );
};
