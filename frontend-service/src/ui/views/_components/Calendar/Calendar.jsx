import React from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';

export const Calendar = ({
  appendTo,
  autoZIndex,
  baseZIndex,
  className,
  dateFormat,
  disabledDates,
  inline,
  inputClassName,
  inputId,
  keepInvalid,
  key,
  maxDate,
  minDate,
  monthNavigator,
  onChange,
  onFocus,
  placeholder,
  readOnlyInput,
  selectionMode,
  showWeek,
  style,
  value,
  yearNavigator,
  yearRange
}) => {
  return (
    <PrimeCalendar
      appendTo={appendTo}
      autoZIndex={autoZIndex}
      baseZIndex={baseZIndex}
      className={className}
      dateFormat={dateFormat}
      disabledDates={disabledDates}
      inline={inline}
      inputClassName={inputClassName}
      inputId={inputId}
      keepInvalid={keepInvalid}
      key={key}
      maxDate={maxDate}
      minDate={minDate}
      monthNavigator={monthNavigator}
      onChange={onChange}
      onFocus={onFocus}
      placeholder={placeholder}
      readOnlyInput={readOnlyInput}
      selectionMode={selectionMode}
      showWeek={showWeek}
      style={style}
      value={value}
      yearNavigator={yearNavigator}
      yearRange={yearRange}
    />
  );
};
