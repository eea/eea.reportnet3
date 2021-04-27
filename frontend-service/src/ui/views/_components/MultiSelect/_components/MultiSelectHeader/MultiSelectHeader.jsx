import { useContext, useRef } from 'react';

import { Checkbox } from 'primereact/checkbox';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

export const MultiSelectHeader = ({
  allChecked,
  checkAllHeader,
  clearButton = true,
  filter,
  filterPlaceholder,
  filterValue,
  headerClassName,
  isPanelVisible,
  notCheckAllHeader,
  onClose,
  onFilter,
  onToggleAll
}) => {
  const resources = useContext(ResourcesContext);

  const filterRef = useRef(null);

  useInputTextFocus(isPanelVisible, filterRef);

  const onFilterEvent = event => {
    if (onFilter) {
      onFilter({ originalEvent: event, query: event.target.value });
    }
  };

  const onToggleAllEvent = event => {
    if (onToggleAll) {
      onToggleAll({ checked: allChecked, originalEvent: event });
    }
  };

  const renderFilterElement = () => {
    if (filter) {
      return (
        <div className="p-multiselect-filter-container">
          <InputText
            className="p-inputtext p-component"
            onChange={event => onFilterEvent(event)}
            placeholder={filterPlaceholder}
            ref={filterRef}
            role="textbox"
            type="text"
            value={filterValue}
          />
          <span className="p-multiselect-filter-icon pi pi-search"></span>
        </div>
      );
    } else
      return (
        <span id="selectAll" className={headerClassName} onClick={event => onToggleAll(event)}>
          {allChecked ? notCheckAllHeader : checkAllHeader}
        </span>
      );
  };

  return (
    <div className="p-multiselect-header" style={{ padding: '0.5rem' }}>
      <Checkbox
        aria-checked={allChecked}
        ariaLabelledBy="selectAll"
        checked={allChecked}
        onChange={event => onToggleAllEvent(event)}
        role="checkbox"
      />

      {renderFilterElement()}

      {clearButton && (
        <button type="button" className="p-multiselect-close p-link" onClick={event => onClose(event)}>
          <span className="p-multiselect-close-icon pi pi-times" />
          <span className="srOnly">{resources.messages['clearFilter']}</span>
        </button>
      )}
    </div>
  );
};
