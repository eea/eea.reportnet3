import React from 'react';

import { Checkbox } from 'primereact/checkbox';
import { InputText } from 'ui/views/_components/InputText';

export const MultiSelectHeader = ({
  allChecked,
  filter,
  filterPlaceholder,
  filterValue,
  onClose,
  onFilter,
  onToggleAll
}) => {
  const onFilterEvent = event => {
    if (onFilter) {
      onFilter({
        originalEvent: event,
        query: event.target.value
      });
    }
  };

  const onToggleAllEvent = event => {
    if (onToggleAll) {
      onToggleAll({
        originalEvent: event,
        checked: allChecked
      });
    }
  };

  const renderFilterElement = () => {
    if (filter) {
      return (
        <div className="p-multiselect-filter-container">
          <InputText
            type="text"
            role="textbox"
            value={filterValue}
            onChange={event => onFilterEvent(event)}
            className="p-inputtext p-component"
            placeholder={filterPlaceholder}
          />
          <span className="p-multiselect-filter-icon pi pi-search"></span>
        </div>
      );
    } else return null;
  };

  return (
    <div className="p-multiselect-header">
      <Checkbox
        aria-checked={allChecked}
        checked={allChecked}
        onChange={event => onToggleAllEvent(event)}
        role="checkbox"
      />
      {renderFilterElement()}
      <button type="button" className="p-multiselect-close p-link" onClick={event => onClose(event)}>
        <span className="p-multiselect-close-icon pi pi-times" />
      </button>
    </div>
  );
};
