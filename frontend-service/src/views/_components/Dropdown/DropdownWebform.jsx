import { useState, useRef, useEffect, useCallback } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';

import './Dropdown.scss';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import FilterUtils from 'views/_functions/PrimeReact/FilterUtils';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import classNames from 'classnames';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import DropdownPanel from './_components/DropdownPanel/DropdownPanel';
import { DropdownItem } from './_components/DropdownItem';
import { Spinner } from 'views/_components/Spinner';
import Tooltip from 'primereact/tooltip';

const DropdownWebform = props => {
  var {
    appendTo = null,
    ariaLabel = null,
    ariaLabelledBy = null,
    autoFocus = false,
    classNameProp = null,
    currentValue = undefined,
    dataKey = null,
    disabled = false,
    editable = false,
    filter = false,
    filterBy = null,
    filterInputAutoFocus = true,
    filterMatchMode = 'contains',
    filterPlaceholder = null,
    id = null,
    inputClassName = null,
    inputId = null,
    isLoadingData = false,
    itemTemplate = null,
    labelProp = null,
    maxLength = null,
    name = null,
    onBlur = () => {},
    onChange = null,
    onContextMenu = null,
    onEmptyList = null,
    onFilterInputChangeBackend = null,
    onMouseDown = null,
    optionLabel = null,
    options = null,
    panelRefClassName = null,
    panelRefStyle = null,
    placeholder = null,
    required = false,
    scrollHeight = '200px',
    showClear = false,
    showFilterClear = false,
    style = null,
    tabIndex = null,
    tooltip = null,
    tooltipOptions = null,
    value = null,
    filterLocale
  } = props;

  const [filterState, setFilterState] = useState(currentValue ? currentValue : '');
  const [searchTimeout, setSearchTimeout] = useState(null);
  const [previousSearchChar, setPreviousSearchChar] = useState('');
  const panelRef = useRef(null);
  const containerRef = useRef(null);
  const focusInputRef = useRef(null);
  const editableInputRef = useRef(null);
  const filterInputRef = useRef(null);
  const nativeSelectRef = useRef(null);
  const itemsWrapperRef = useRef(null);

  let documentClickListener,
    selfClick,
    searchValue,
    hideTimeout,
    currentSearchChar,
    editableInputClick,
    overlayClick,
    expeditableInputClick,
    selectedOptionUpdated;

  const onKeyPress = event => {
    if (props.onKeyPress && event.which === 13) {
      props.onKeyPress(event);
    }
  };

  const onClick = event => {
    if (disabled) {
      return;
    }

    if (documentClickListener) {
      selfClick = true;
    }

    let clearClick =
      DomHandler.hasClass(event.target, 'p-dropdown-clear-icon') ||
      DomHandler.hasClass(event.target, 'p-dropdown-clear-filter-icon');
    if (!overlayClick && !editableInputClick && !clearClick) {
      focusInputRef.current.focus();

      if (panelRef.current && panelRef.current.offsetParent) {
        hide();
      } else {
        show();

        if (filter && filterInputAutoFocus) {
          setTimeout(() => {
            filterInputRef.current.focus();
          }, 200);
        }
      }
    }

    if (editableInputClick) {
      expeditableInputClick = false;
    }
  };

  const panelRefClick = () => {
    overlayClick = true;
  };

  const onInputFocus = event => {
    DomHandler.addClass(containerRef.current, 'p-focus');
  };

  const onInputBlur = event => {
    onBlur();
    DomHandler.removeClass(containerRef.current, 'p-focus');
  };

  const onUpKey = event => {
    if (options) {
      const selectedItemIndex = findOptionIndex(value);

      if (selectedItemIndex !== -1) {
        const prevItem = findPrevVisibleItem(selectedItemIndex);

        if (prevItem) {
          selectItem({
            originalEvent: event,
            option: prevItem
          });
        }
      }
    }

    event.preventDefault();
  };

  const onDownKey = event => {
    if (options) {
      if (!panelRef.current.offsetParent && event.altKey) {
        show();
      } else {
        let selectedItemIndex = findOptionIndex(value);
        let nextItem = findNextVisibleItem(selectedItemIndex);

        if (nextItem) {
          selectItem({
            originalEvent: event,
            option: nextItem
          });
        }
      }
    }

    event.preventDefault();
  };

  const onInputKeyDown = event => {
    switch (event.which) {
      //down
      case 40:
        onDownKey(event);
        break;
      //up
      case 38:
        onUpKey(event);
        break;
      //space
      case 32:
        if (!panelRef.current.offsetParent) {
          show();
          event.preventDefault();
        }
        break;
      //enter
      case 13:
        hide();
        props.onKeyPress(event);
        event.preventDefault();
        break;
      //escape and tab
      case 27:
      case 9:
        hide();
        break;
      default:
        search(event);
        break;
    }
  };

  const search = event => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }

    const char = String.fromCharCode(event.keyCode);
    setPreviousSearchChar(currentSearchChar);
    currentSearchChar = char;

    if (previousSearchChar === currentSearchChar) searchValue = currentSearchChar;
    else searchValue = searchValue ? searchValue + char : char;

    let searchIndex = value ? findOptionIndex(value) : -1;
    let newOption = searchOption(++searchIndex);

    if (newOption) {
      selectItem({
        originalEvent: event,
        option: newOption
      });
      selectedOptionUpdated = true;
    }

    setSearchTimeout(
      setTimeout(() => {
        searchValue = null;
      }, 250)
    );
  };

  const searchOption = index => {
    let option;

    if (searchValue) {
      option = searchOptionInRange(index, options.length);

      if (!option) {
        option = searchOptionInRange(0, index);
      }
    }

    return option;
  };

  const searchOptionInRange = (start, end) => {
    for (let i = start; i < end; i++) {
      let opt = options[i];
      let label = getOptionLabel(opt).toString().toLowerCase();
      if (label.startsWith(searchValue.toLowerCase())) {
        return opt;
      }
    }

    return null;
  };

  const findNextVisibleItem = index => {
    let i = index + 1;
    if (i === options.length) {
      return null;
    }

    let option = options[i];

    if (option.disabled) {
      return findNextVisibleItem(i);
    }

    if (hasFilter()) {
      if (filterFunc(option)) return option;
      else return findNextVisibleItem(i);
    } else {
      return option;
    }
  };

  const findPrevVisibleItem = index => {
    let i = index - 1;
    if (i === -1) {
      return null;
    }

    let option = options[i];

    if (option.disabled) {
      return findPrevVisibleItem(i);
    }

    if (hasFilter()) {
      if (filterFunc(option)) return option;
      else return findPrevVisibleItem(i);
    } else {
      return option;
    }
  };

  const onEditableInputClick = event => {
    editableInputClick = true;
    bindDocumentClickListener();
  };

  const onEditableInputChange = event => {
    onChange({
      originalEvent: event.originalEvent,
      value: event.target.value,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: name,
        id: id,
        value: event.target.value
      }
    });
  };

  const onEditableInputFocus = event => {
    DomHandler.addClass(containerRef, 'p-focus');
    hide();
  };

  const onOptionClick = event => {
    const option = event.option;

    if (!option.disabled) {
      selectItem(event);
      focusInputRef.current.focus();
    }

    setTimeout(() => {
      hide();
    }, 100);
  };

  const onFilterInputChange = event => {
    if (onFilterInputChangeBackend) {
      onFilterInputChangeBackend(event.target.value);
      setFilterState(event.target.value);
    } else {
      setFilterState(event.target.value);
    }
  };

  const onFilterInputKeyDown = event => {
    switch (event.which) {
      //down
      case 40:
        onDownKey(event);
        break;
      //up
      case 38:
        onUpKey(event);
        break;
      //enter
      case 13:
        hide();
        event.preventDefault();
        break;
      default:
        break;
    }
  };

  const clear = event => {
    onChange({
      originalEvent: event,
      value: null,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: name,
        id: id,
        value: null
      }
    });
    updateEditableLabel();
  };

  const clearFilter = () => {
    if (onFilterInputChangeBackend) {
      onFilterInputChangeBackend('');
      setFilterState('');
      setTimeout(() => {
        filterInputRef.current.focus();
      }, 200);
    } else {
      setFilterState('');
      filterInputRef.current.focus();
    }
  };

  const selectItem = event => {
    let currentSelectedOption = findOption(value);
    if (currentSelectedOption !== event.option) {
      updateEditableLabel(event.option);
      onChange({
        originalEvent: event.originalEvent,
        value: optionLabel ? event.option : event.option.value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: name,
          id: id,
          value: optionLabel ? event.option : event.option.value
        }
      });
    }
  };

  const findOptionIndex = value => {
    let index = -1;
    if (options) {
      for (let i = 0; i < options.length; i++) {
        let optionValue = optionLabel ? options[i] : options[i].value;
        if ((value === null && optionValue == null) || ObjectUtils.equals(value, optionValue, dataKey)) {
          index = i;
          break;
        }
      }
    }

    return index;
  };

  const findOption = value => {
    let index = findOptionIndex(value);
    return index !== -1 ? options[index] : null;
  };

  const show = useCallback(() => {
    if (panelRef.current) {
      panelRef.current.style.zIndex = String(DomHandler.generateZIndex());
      panelRef.current.style.display = 'block';

      setTimeout(() => {
        DomHandler.addClass(panelRef.current, 'p-input-overlay-visible');
        DomHandler.removeClass(panelRef.current, 'p-input-overlay-hidden');
      }, 1);

      alignPanel();
      bindDocumentClickListener();
    }
  }, [panelRef]);

  const hide = () => {
    if (panelRef && panelRef.current) {
      DomHandler.addClass(panelRef.current, 'p-input-overlay-hidden');
      DomHandler.removeClass(panelRef.current, 'p-input-overlay-visible');
      unbindDocumentClickListener();
      clearClickState();

      hideTimeout = setTimeout(() => {
        if (panelRef.current) {
          panelRef.current.style.display = 'none';
          DomHandler.removeClass(panelRef.current, 'p-input-overlay-hidden');
        }
      }, 150);
    }
  };

  const alignPanel = () => {
    if (appendTo) {
      panelRef.current.style.minWidth = DomHandler.getWidth(containerRef?.current) + 'px';
      DomHandler.absolutePosition(panelRef.current, containerRef.current);
    } else {
      DomHandler.relativePosition(panelRef.current, containerRef.current);
    }
  };

  const bindDocumentClickListener = () => {
    if (!documentClickListener) {
      documentClickListener = () => {
        if (!selfClick && !overlayClick) {
          hide();
        }

        clearClickState();
      };

      document.addEventListener('click', documentClickListener);
    }
  };

  const unbindDocumentClickListener = () => {
    if (documentClickListener) {
      document.removeEventListener('click', documentClickListener);
      documentClickListener = null;
    }
  };

  const clearClickState = () => {
    selfClick = false;
    editableInputClick = false;
    overlayClick = false;
  };

  const updateEditableLabel = option => {
    if (editableInputRef.current) {
      editableInputRef.current.value = option ? getOptionLabel(option) : value || '';
    }
  };

  const filterFunc = options => {
    let filterValue = filterState.trim().toLocaleLowerCase(filterLocale);
    let searchFields = filterBy ? filterBy.split(',') : [optionLabel || 'label'];
    let items = FilterUtils.filter(options, searchFields, filterValue, filterMatchMode, filterLocale);

    return items && items.length ? items : null;
  };

  const hasFilter = () => {
    return filterState && filterState.trim().length > 0;
  };

  const renderHiddenSelect = selectedOption => {
    let placeHolderOption = <option value="">{placeholder}</option>;
    let option = selectedOption ? <option value={selectedOption.value}>{getOptionLabel(selectedOption)}</option> : null;

    return (
      <div className="p-hidden-accessible p-dropdown-hidden-select">
        <select
          aria-hidden="true"
          aria-label={name}
          id={name}
          name={name}
          ref={nativeSelectRef}
          required={required}
          tabIndex="-1">
          {placeHolderOption}
          {option}
        </select>
        <label className="srOnly" htmlFor={name}>
          {name}
        </label>
      </div>
    );
  };

  const renderKeyboardHelper = label => {
    return (
      <div className={`p-hidden-accessible ${inputClassName}`}>
        <input
          aria-label={ariaLabel}
          aria-labelledby={ariaLabelledBy}
          className={label ? 'p-filled' : ''}
          disabled={disabled}
          id={inputId}
          onBlur={onInputBlur}
          onFocus={onInputFocus}
          onKeyDown={onInputKeyDown}
          readOnly={true}
          ref={focusInputRef}
          tabIndex={tabIndex}
          type="text"
        />
        <label htmlFor={inputId}>{label}</label>
      </div>
    );
  };

  const renderLabel = (label, selectedOption) => {
    if (editable) {
      let value = label || value || '';

      return (
        <input
          aria-label={ariaLabel}
          aria-labelledby={ariaLabelledBy}
          className="p-dropdown-label p-inputtext"
          defaultValue={value}
          disabled={disabled}
          maxLength={maxLength}
          onBlur={onInputBlur}
          onClick={onEditableInputClick}
          onFocus={onEditableInputFocus}
          onInput={onEditableInputChange}
          placeholder={placeholder}
          ref={editableInputRef}
          type="text"
        />
      );
    } else {
      let className = classNames('p-dropdown-label p-inputtext', {
        'p-placeholder': label === null && placeholder,
        'p-dropdown-label-empty': label === null && !placeholder
      });
      return (
        <label className={className} style={{ fontStyle: isNull(selectedOption) ? 'italic' : 'inherit' }}>
          <span>{label || placeholder || ''}</span>
          {required && isNull(selectedOption) ? (
            <FontAwesomeIcon
              aria-label="required"
              icon={AwesomeIcons('infoCircle')}
              style={{ float: 'right', color: 'var(--errors)' }}
            />
          ) : null}
          {selectedOption ? (
            selectedOption.fieldTypeIcon ? (
              <FontAwesomeIcon
                icon={AwesomeIcons(selectedOption.fieldTypeIcon)}
                role="presentation"
                style={{ float: 'right', marginTop: '2px' }}
              />
            ) : null
          ) : null}
        </label>
      );
    }
  };

  const renderClearIcon = () => {
    if (value != null && showClear && !disabled) {
      return <i className="p-dropdown-clear-icon pi pi-times" onClick={clear}></i>;
    } else {
      return null;
    }
  };

  const renderDropdownIcon = () => {
    return (
      <div className="p-dropdown-trigger">
        <span className="p-dropdown-trigger-icon pi pi-chevron-down p-clickable"></span>
      </div>
    );
  };

  const renderItems = selectedOption => {
    let items = options;

    if (items && hasFilter()) {
      items = filterFunc(items);
    }

    if (items) {
      return items.map(option => {
        let optionLabel = getOptionLabel(option);
        return (
          <DropdownItem
            disabled={option.disabled}
            key={getOptionKey(option)}
            label={optionLabel}
            onClick={onOptionClick}
            option={option}
            selected={selectedOption === option}
            template={itemTemplate}
          />
        );
      });
    } else {
      if (onEmptyList) {
        onEmptyList();
      }
      return null;
    }
  };

  const renderFilter = () => {
    if (filter) {
      return (
        <div className="p-dropdown-filter-containerRef">
          <input
            aria-label={ariaLabel}
            aria-labelledby={ariaLabelledBy}
            autoComplete="off"
            className="p-dropdown-filter p-inputtext p-component"
            onChange={onFilterInputChange}
            onKeyDown={onFilterInputKeyDown}
            placeholder={filterPlaceholder}
            ref={filterInputRef}
            type="text"
            value={filterState}
          />
          {showFilterClear && !isEmpty(filter) ? (
            <span className="p-dropdown-filter-clear-icon pi pi-times" onClick={clearFilter}></span>
          ) : null}
          <span className="p-dropdown-filter-icon pi pi-search"></span>
        </div>
      );
    } else {
      return null;
    }
  };

  const getOptionLabel = option => {
    return optionLabel ? ObjectUtils.resolveFieldData(option, optionLabel) : option.label;
  };

  const getOptionKey = option => {
    return dataKey ? ObjectUtils.resolveFieldData(option, dataKey) : getOptionLabel(option);
  };

  const checkValidity = () => {
    return nativeSelectRef.checkValidity;
  };

  // Component willMount/willUnmount with UseEffect hook
  useEffect(() => {
    if (autoFocus && focusInputRef.current) {
      focusInputRef.current.focus();
    }

    if (tooltip) {
      renderTooltip();
    }

    if (nativeSelectRef) nativeSelectRef.current.selectedIndex = 1;

    return () => {
      unbindDocumentClickListener();

      if (tooltip) {
        tooltip.destroy();
        tooltip = null;
      }

      if (hideTimeout) {
        clearTimeout(hideTimeout);
        hideTimeout = null;
      }
    };
  }, []);

  useEffect(() => {
    if (filter && panelRef.current) alignPanel();
  }, [filter, panelRef]);

  useEffect(() => {
    if (panelRef.current && panelRef.current && panelRef.current.offsetParent) {
      let highlightItem = DomHandler.findSingle(panelRef.current, 'li.p-highlight');
      if (highlightItem && panelRef.current.itemsWrapper) {
        DomHandler.scrollInView(panelRef.current.itemsWrapper, highlightItem);
      }
    }
  }, [panelRef.current]);

  useEffect(() => {
    if (nativeSelectRef.current) nativeSelectRef.current.selectedIndex = 1;
  }, [nativeSelectRef.current]);

  useEffect(() => {
    if (tooltip) {
      tooltip.updateContent(tooltip);
    } else {
      if (containerRef && containerRef.current) renderTooltip();
    }
  }, [tooltip, containerRef]);

  const renderTooltip = () => {
    tooltip = new Tooltip({
      target: containerRef?.current,
      content: tooltip,
      options: tooltipOptions
    });
  };

  let className = classNames('p-dropdown p-component', classNameProp, {
    'p-disabled': disabled,
    'p-dropdown-clearable': showClear && !disabled
  });
  let selectedOption = findOption(value);
  let label = selectedOption ? getOptionLabel(selectedOption) : null;

  let hiddenSelect = renderHiddenSelect(selectedOption);
  let keyboardHelper = renderKeyboardHelper(labelProp);
  let labelElement = isLoadingData ? (
    <Spinner style={{ top: 0, width: '25px', height: '25px' }} />
  ) : (
    renderLabel(label, selectedOption)
  );
  let dropdownIcon = renderDropdownIcon();
  let items = renderItems(selectedOption);
  let filterElement = renderFilter();
  let clearIcon = renderClearIcon();

  if (editable && editableInputRef.current) {
    let value = labelProp || value || '';
    editableInputRef.value = value;
  }

  return (
    <div
      className={className}
      id={id}
      onClick={onClick}
      onContextMenu={onContextMenu}
      onKeyPress={onKeyPress}
      onMouseDown={onMouseDown}
      ref={containerRef}
      style={style}>
      {keyboardHelper}
      {hiddenSelect}
      {labelElement}
      {clearIcon}
      {dropdownIcon}
      <DropdownPanel
        appendTo={appendTo}
        filter={filterElement}
        onClick={panelRefClick}
        panelRefClassName={panelRefClassName}
        panelRefStyle={panelRefStyle}
        ref={panelRef}
        scrollHeight={scrollHeight}
        itemsWrapperRef={itemsWrapperRef}>
        {items}
      </DropdownPanel>
    </div>
  );
};

// For performance debugging with ReactDevTools Profiler
DropdownWebform.displayName = 'Dropdown';

export default DropdownWebform;
