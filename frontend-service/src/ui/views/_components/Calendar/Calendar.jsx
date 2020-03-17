import React from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';

export const Calendar = ({
  className,
  dateFormat,
  inline,
  inputClassName,
  inputId,
  key,
  maxDate,
  minDate,
  monthNavigator,
  onChange,
  placeholder,
  readOnlyInput,
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
      inputClassName={inputClassName}
      inputId={inputId}
      key={key}
      maxDate={maxDate}
      minDate={minDate}
      monthNavigator={monthNavigator}
      onChange={onChange}
      placeholder={placeholder}
      readOnlyInput={readOnlyInput}
      selectionMode={selectionMode}
      showWeek={showWeek}
      value={value}
      yearNavigator={yearNavigator}
      yearRange={yearRange}
    />
  );
};
