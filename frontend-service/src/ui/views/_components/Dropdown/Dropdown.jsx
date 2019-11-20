import React from 'react';
import { Dropdown as PrimeDropdown } from 'primereact/dropdown';

export const Dropdown = ({
  className,
  filter,
  filterBy,
  filterPlaceholder,
  itemTemplate,
  onChange,
  onMouseDown,
  optionLabel,
  options,
  placeholder,
  scrollHeight,
  showClear,
  style,
  value
}) => {
  return (
    <PrimeDropdown
      className={className}
      filter={filter}
      filterBy={filterBy}
      filterPlaceholder={filterPlaceholder}
      itemTemplate={itemTemplate}
      onChange={onChange}
      onMouseDown={onMouseDown}
      optionLabel={optionLabel}
      options={options}
      placeholder={placeholder}
      scrollHeight={scrollHeight}
      showClear={showClear}
      style={style}
      value={value}
    />
  );
};
