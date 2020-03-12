import React from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';

export const Calendar = ({
  className,
  dateFormat,
  inline,
  key,
  maxDate,
  minDate,
  monthNavigator,
  onChange,
  selectionMode,
  showWeek,
  value,
  yearNavigator,
  yearRange
}) => {
  return (
    <PrimeCalendar
      className={className}
      dateFormat={dateFormat}
      inline={inline}
      key={key}
      maxDate={maxDate}
      minDate={minDate}
      monthNavigator={monthNavigator}
      onChange={onChange}
      selectionMode={selectionMode}
      showWeek={showWeek}
      value={value}
      yearNavigator={yearNavigator}
      yearRange={yearRange}
    />
  );
};
