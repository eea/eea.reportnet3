import { useContext, useRef } from 'react';
import uniqueId from 'lodash/uniqueId';

import styles from './MultiSelectHeader.module.scss';

import { Checkbox } from 'views/_components/Checkbox';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

export const MultiSelectHeader = ({
  allChecked,
  checkAllHeader,
  clearButton = true,
  filter,
  filterPlaceholder,
  filterValue,
  headerClassName,
  id,
  isPanelVisible,
  notCheckAllHeader,
  onClose,
  onFilter,
  onToggleAll
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const filterRef = useRef(null);

  useInputTextFocus(isPanelVisible, filterRef);

  const getClassNameAllChecked = () => {
    if (allChecked) {
      return `${headerClassName} ${styles.allItemsSelected}`;
    }

    return headerClassName;
  };

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

  const renderClearButton = () => {
    if (clearButton) {
      return (
        <button className="p-multiselect-close p-link" onClick={event => onClose(event)} type="button">
          <span className="p-multiselect-close-icon pi pi-times" id={`clearFilter_${id}`} />
          <span className="srOnly" htmlFor={`clearFilter_${id}`}>
            {resourcesContext.messages['clearFilter']}
          </span>
        </button>
      );
    }
  };

  const renderFilterElement = () => {
    if (filter) {
      return (
        <div className="p-multiselect-filter-container">
          <InputText
            className="p-inputtext p-component"
            id={uniqueId('multiselectFilter_')}
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
        <span
          className={getClassNameAllChecked()}
          id={`selectAllFilter_${id}`}
          onClick={event => onToggleAllEvent(event)}>
          {allChecked ? notCheckAllHeader : checkAllHeader}
        </span>
      );
  };

  return (
    <div className="p-multiselect-header" style={{ padding: '0.5rem' }}>
      <Checkbox
        aria-checked={allChecked}
        ariaLabel={`selectAll_${id}`}
        checked={allChecked}
        inputId={`selectAll_${id}`}
        onChange={event => onToggleAllEvent(event)}
        role="checkbox"
      />

      {renderFilterElement()}
      {renderClearButton()}
    </div>
  );
};
