import { forwardRef } from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';

export const Calendar = forwardRef((props, _) => {
  const {
    appendTo,
    autoZIndex,
    baseZIndex,
    className,
    dateFormat,
    disabledDates,
    inline,
    inputClassName,
    inputId,
    inputRef,
    keepInvalid,
    key,
    locale,
    maxDate,
    minDate,
    monthNavigator,
    onBlur,
    onChange,
    onFocus,
    onSelect,
    onTodayButtonClick,
    placeholder,
    readOnlyInput,
    selectionMode,
    showButtonBar,
    showSeconds = false,
    showTime = false,
    showWeek,
    style,
    todayButtonClassName,
    value,
    yearNavigator,
    yearRange
  } = props;
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
      locale={locale}
      maxDate={maxDate}
      minDate={minDate}
      monthNavigator={monthNavigator}
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onSelect={onSelect}
      onTodayButtonClick={onTodayButtonClick}
      placeholder={placeholder}
      readOnlyInput={readOnlyInput}
      ref={inputRef}
      selectionMode={selectionMode}
      showButtonBar={showButtonBar}
      showSeconds={showSeconds}
      showTime={showTime}
      showWeek={showWeek}
      style={style}
      todayButtonClassName={todayButtonClassName}
      value={value}
      yearNavigator={yearNavigator}
      yearRange={yearRange}
    />
  );
});
