import { useEffect, useRef, useState } from 'react';
import isNil from 'lodash/isNil';
import PropTypes from 'prop-types';
import classNames from 'classnames';

import './ListBox.scss';

import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';

import { ListBoxItem } from './_components/ListBoxItem';
import { ListBoxHeader } from './_components/ListBoxHeader';
import { Spinner } from 'views/_components/Spinner';
import Tooltip from 'primereact/tooltip';

const ListBox = ({
  ariaLabel = null,
  ariaLabelledBy = null,
  className = null,
  dataKey = null,
  disabled = null,
  filterProp = false,
  id = null,
  itemTemplate = null,
  listStyle = null,
  metaKeySelection = false,
  multiple = false,
  name = null,
  onChange = null,
  optionLabel = null,
  options = null,
  style = null,
  spinner = false,
  tabIndex = '0',
  title = '',
  tooltip = null,
  tooltipOptions = null,
  value = null
}) => {
  const inputElement = useRef();
  const [filter, setFilter] = useState('');
  const [optionTouched, setOptionTouched] = useState(false);

  useEffect(() => {
    if (!isNil(tooltip)) {
      renderTooltip();
    }
  }, []);

  const renderTooltip = () => {
    return new Tooltip({
      target: inputElement.current.element,
      content: tooltip,
      options: tooltipOptions
    });
  };

  const onOptionClick = event => {
    if (disabled) {
      return;
    }

    if (multiple) onOptionClickMultiple(event.originalEvent, event.option);
    else onOptionClickSingle(event.originalEvent, event.option);

    setOptionTouched(false);
  };

  const onOptionTouchEnd = (event, option) => {
    if (disabled) {
      return;
    }

    setOptionTouched(true);
  };

  const onOptionClickSingle = (event, option) => {
    let selected = isSelected(option);
    let valueChanged = false;
    let value = null;
    let metaSelection = optionTouched ? false : metaKeySelection;

    if (metaSelection) {
      let metaKey = event.metaKey || event.ctrlKey;

      if (selected) {
        if (metaKey) {
          value = null;
          valueChanged = true;
        }
      } else {
        value = getOptionValue(option);
        valueChanged = true;
      }
    } else {
      value = selected ? null : getOptionValue(option);
      valueChanged = true;
    }

    if (valueChanged) {
      updateModel(event, value);
    }
  };

  const onOptionClickMultiple = (event, option) => {
    let selected = isSelected(option);
    let valueChanged = false;
    let values = null;
    let metaSelection = optionTouched ? false : metaKeySelection;

    if (metaSelection) {
      let metaKey = event.metaKey || event.ctrlKey;

      if (selected) {
        if (metaKey) values = removeOption(option);
        else values = [getOptionValue(option)];

        valueChanged = true;
      } else {
        values = metaKey ? value || [] : [];
        values = [...values, getOptionValue(option)];
        valueChanged = true;
      }
    } else {
      if (selected) values = removeOption(option);
      else values = [...(value || []), getOptionValue(option)];

      valueChanged = true;
    }

    if (valueChanged) {
      onChange({
        originalEvent: event,
        value: values,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name,
          id,
          value: values
        }
      });
    }
  };

  const onFilter = event => {
    setFilter(event.query);
  };

  const updateModel = (event, value) => {
    if (onChange) {
      onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name,
          id,
          value
        }
      });
    }
  };

  const removeOption = option => {
    return value.filter(val => !ObjectUtils.equals(val, getOptionValue(option), dataKey));
  };

  const isSelected = option => {
    let selected = false;
    let optionValue = getOptionValue(option);
    if (multiple) {
      if (value) {
        for (let val of value) {
          if (ObjectUtils.equals(val, optionValue, dataKey)) {
            selected = true;
            break;
          }
        }
      }
    } else {
      if (!option.disabled) {
        selected = ObjectUtils.equals(value, optionValue, dataKey);
      } else {
        selected = false;
      }
    }

    return selected;
  };

  const filterListBox = option => {
    let filterValue = filter.trim().toLowerCase();
    let optionLabel = getOptionLabel(option);

    return optionLabel.toLowerCase().indexOf(filterValue.toLowerCase()) > -1;
  };

  const hasFilter = () => {
    return !isNil(filter) && filter.trim().length > 0;
  };

  const getOptionValue = option => {
    return optionLabel ? option : option.value;
  };

  const getOptionLabel = option => {
    return optionLabel ? ObjectUtils.resolveFieldData(option, optionLabel) : option.label;
  };

  const renderListBox = () => {
    let classes = classNames('p-listbox p-inputtext p-component', className, {
      'p-disabled': disabled
    });
    let items = options;
    let header;

    if (options) {
      if (hasFilter()) {
        items = items.filter(option => {
          return filterListBox(option);
        });
      }

      items = items.map((option, index) => {
        let optionLabel = getOptionLabel(option);
        return (
          <ListBoxItem
            disabled={option.disabled}
            key={optionLabel}
            label={optionLabel}
            onClick={option.disabled ? null : onOptionClick}
            onTouchEnd={e => onOptionTouchEnd(e, option, index)}
            option={option}
            selected={isSelected(option)}
            tabIndex={tabIndex}
            template={itemTemplate}
          />
        );
      });
    }

    if (filterProp) {
      header = <ListBoxHeader disabled={disabled} filter={filter} onFilter={onFilter} />;
    }

    return (
      <div className={classes} id={id} ref={inputElement} style={style}>
        {header}
        {title && (
          <div className="p-listbox-title-wrapper">
            <span className="p-listbox-title">{title}</span>
          </div>
        )}
        <div className="p-listbox-list-wrapper">
          {!items && spinner ? (
            <div className="listbox-spinner-wrapper" style={listStyle}>
              <Spinner style={{ top: 0, left: 0, maxWidth: '15%' }} />
            </div>
          ) : (
            <ul
              aria-label={ariaLabel}
              aria-multiselectable={multiple}
              className="p-listbox-list"
              role="listbox"
              style={listStyle}>
              {items}
            </ul>
          )}
        </div>
      </div>
    );
  };

  return renderListBox();
};

ListBox.propTypes = {
  id: PropTypes.string,
  value: PropTypes.any,
  options: PropTypes.array,
  optionLabel: PropTypes.string,
  itemTemplate: PropTypes.func,
  style: PropTypes.object,
  listStyle: PropTypes.object,
  className: PropTypes.string,
  dataKey: PropTypes.string,
  multiple: PropTypes.bool,
  metaKeySelection: PropTypes.bool,
  filter: PropTypes.bool,
  tabIndex: PropTypes.string,
  tooltip: PropTypes.string,
  tooltipOptions: PropTypes.object,
  ariaLabel: PropTypes.string,
  ariaLabelledBy: PropTypes.string,
  onChange: PropTypes.func
};

export { ListBox };
