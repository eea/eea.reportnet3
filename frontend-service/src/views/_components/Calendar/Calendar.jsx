import { forwardRef } from 'react';

import { Calendar as PrimeCalendar } from 'primereact/calendar';
import { isNil } from 'lodash';

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
    maxDate,
    minDate,
    monthNavigator,
    onBlur,
    onChange,
    onFocus,
    onSelect,
    onTodayButtonClick,
    panelClassName,
    placeholder,
    readOnlyInput,
    selectionMode,
    selectableYears = 10,
    showButtonBar,
    showSeconds = false,
    showTime = false,
    showWeek = false,
    style,
    todayButtonClassName,
    value,
    yearNavigator,
    yearRange
  } = props;

  const locale = {
    firstDayOfWeek: 1,
    dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
    dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
    monthNames: [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December'
    ],
    monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    today: 'Today',
    clear: 'Clear',
    weekHeader: 'Wk'
  };

  const yearRangeValue =
    yearNavigator && isNil(yearRange)
      ? `${new Date().getFullYear() - selectableYears}:${new Date().getFullYear() + selectableYears}`
      : yearRange;

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
      locale={locale}
      maxDate={maxDate}
      minDate={minDate}
      monthNavigator={monthNavigator}
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onSelect={onSelect}
      onTodayButtonClick={onTodayButtonClick}
      panelClassName={panelClassName}
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
      yearRange={yearRangeValue}
    />
  );
});
