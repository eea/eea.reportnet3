import { Fragment, useCallback, useEffect, useRef, useState } from 'react';

import classNames from 'classnames';

import styles from './MultiSelect.module.scss';

import { MultiSelectHeader } from './_components/MultiSelectHeader';
import { MultiSelectItem } from './_components/MultiSelectItem';
import { MultiSelectPanel } from './_components/MultiSelectPanel';
import { Spinner } from 'views/_components/Spinner';

import Tooltip from 'primereact/tooltip';

import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import MultiSelectUtils from './_functions/MultiSelectUtils';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

const MultiSelectWebform = props => {
  var {
    addSpaceAfterSeparator = true,
    appendTo = null,
    ariaLabelledBy = null,
    checkAllHeader = null,
    classNameProp = null,
    clearButton = true,
    dataKey = null,
    disabled = false,
    filterBy = null,
    filterMatchMode = 'contains',
    filterPlaceholder = null,
    fixedPlaceholder = false,
    hasSelectedItemsLabel = true,
    headerClassName = null,
    id = null,
    inputClassName = null,
    inputId = null,
    isFilter = false,
    isLoadingData = false,
    itemTemplate = null,
    labelProp = null,
    maxSelectedLabels = 3,
    notCheckAllHeader = null,
    onBlur = null,
    onChange = null,
    onFilterInputChangeBackend = null,
    onFocus = null,
    optionLabel = null,
    optionValue = null,
    options = null,
    panelClassName = null,
    placeholder = null,
    scrollHeight = '200px',
    selectedItemTemplate = null,
    selectedItemsLabel = '{0} items selected',
    style = null,
    tabIndex = '0',
    tooltip = null,
    tooltipOptions = null,
    value = null,
    valuesSeparator = ',',
    name
  } = props;

  const [filterState, setFilterState] = useState('');
  const [isPanelVisible, setIsPanelVisible] = useState(false);
  const panelRef = useRef(null);
  const containerRef = useRef(null);
  const focusInputRef = useRef(null);

  let documentClickListener, selfClick, hideTimeout, panelClick;

  const onOptionClick = event => {
    let optionValue = getOptionValue(event.option);
    let selectionIndex = findSelectionIndex(optionValue);
    let newValue;

    if (selectionIndex !== -1) newValue = value.filter((val, i) => i !== selectionIndex);
    else newValue = [...(value || []), optionValue];

    updateModel(event.originalEvent, newValue);
  };

  const onOptionKeyDown = event => {
    let listItem = event.originalEvent.currentTarget;

    switch (event.originalEvent.which) {
      //down
      case 40:
        var nextItem = findNextItem(listItem);
        if (nextItem) {
          nextItem.focus();
        }

        event.originalEvent.preventDefault();
        break;

      //up
      case 38:
        var prevItem = findPrevItem(listItem);
        if (prevItem) {
          prevItem.focus();
        }

        event.originalEvent.preventDefault();
        break;

      //enter
      case 13:
        onOptionClick(event);
        event.originalEvent.preventDefault();
        break;

      default:
        break;
    }
  };

  const findNextItem = item => {
    let nextItem = item.nextElementSibling;

    if (nextItem) return !DomHandler.hasClass(nextItem, 'p-multiselect-item') ? findNextItem(nextItem) : nextItem;
    else return null;
  };

  const findPrevItem = item => {
    let prevItem = item.previousElementSibling;

    if (prevItem) return !DomHandler.hasClass(prevItem, 'p-multiselect-item') ? findPrevItem(prevItem) : prevItem;
    else return null;
  };

  const onClick = () => {
    if (disabled) {
      return;
    }

    if (documentClickListener) {
      selfClick = true;
    }
    if (!panelClick) {
      if (panelRef && panelRef.current.element && panelRef.current.element.offsetParent) {
        hide();
      } else {
        focusInputRef.current.focus();
        show();
      }
    }
  };

  const onToggleAll = event => {
    let newValue;

    if (event.checked) {
      newValue = [];
    } else {
      options = hasFilter() ? filterOptions(options) : options;
      if (options) {
        newValue = [];
        for (let option of options) {
          newValue.push(getOptionValue(option));
        }
      }
    }

    updateModel(event.originalEvent, newValue);
  };

  const updateModel = (event, value) => {
    if (onChange) {
      onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: name,
          id: id,
          value: value
        }
      });
    }
  };

  const onFilter = event => {
    if (onFilterInputChangeBackend) {
      onFilterInputChangeBackend(event.query);
      setFilterState(event.query);
    } else {
      setFilterState(event.query);
    }
  };

  const onPanelClick = () => {
    panelClick = true;
  };

  const show = useCallback(() => {
    if (panelRef.current.element) {
      if (options && options.length) {
        panelRef.current.element.style.zIndex = String(DomHandler.generateZIndex());
        panelRef.current.element.style.display = 'block';
        setTimeout(() => {
          DomHandler.addClass(panelRef.current.element, 'p-input-overlay-visible');
          DomHandler.removeClass(panelRef.current.element, 'p-input-overlay-hidden');
        }, 1);

        alignPanel();
        bindDocumentClickListener();
        setIsPanelVisible(true);
      }
    }
  }, [panelRef]);

  const hide = () => {
    DomHandler.addClass(panelRef.current.element, 'p-input-overlay-hidden');
    DomHandler.removeClass(panelRef.current.element, 'p-input-overlay-visible');
    unbindDocumentClickListener();
    clearClickState();

    setTimeout(() => {
      if (panelRef.current.element) {
        panelRef.current.element.style.display = 'none';
        DomHandler.removeClass(panelRef.current.element, 'p-input-overlay-hidden');
        clearFilter();
        setIsPanelVisible(false);
      }
    }, 150);
  };

  const alignPanel = () => {
    if (appendTo) {
      panelRef.current.element.style.minWidth = DomHandler.getWidth(containerRef.current) + 'px';
      DomHandler.absolutePosition(panelRef.current.element, containerRef.current);
    } else {
      DomHandler.relativePosition(panelRef.current.element, containerRef.current);
    }
  };

  const onCloseClick = event => {
    hide();
    event.preventDefault();
    event.stopPropagation();
  };

  const findSelectionIndex = val => {
    let index = -1;
    if (value) {
      for (let i = 0; i < value.length; i++) {
        if (ObjectUtils.equals(value[i], val, dataKey)) {
          index = i;
          break;
        }
      }
    }

    return index;
  };

  const isSelected = option => {
    return findSelectionIndex(getOptionValue(option)) !== -1;
  };

  const findLabelByValue = val => {
    let label = null;
    if (options) {
      for (let i = 0; i < options.length; i++) {
        let option = options[i];
        let optionValue = getOptionValue(option);

        if (ObjectUtils.equals(optionValue, val)) {
          label = getOptionLabel(option);
          break;
        }
      }
    }

    return label;
  };

  const onFocusFunction = event => {
    DomHandler.addClass(containerRef.current, 'p-focus');

    if (onFocus) {
      onFocus(event);
    }
  };

  const onBlurFunction = event => {
    DomHandler.removeClass(containerRef.current, 'p-focus');

    if (onBlur) {
      onBlur(event);
    }
  };

  const bindDocumentClickListener = () => {
    if (!documentClickListener) {
      documentClickListener = () => {
        if (!selfClick) {
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

  // Component willMount/willUnmount with UseEffect hook
  useEffect(() => {
    if (tooltip) {
      renderTooltip();
    }

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

  const clearClickState = () => {
    selfClick = false;
    panelClick = false;
  };

  const clearFilter = () => {
    if (onFilterInputChangeBackend) {
      onFilterInputChangeBackend('');
      setFilterState('');
    } else {
      setFilterState('');
    }
  };

  const hasFilter = () => {
    return filterState && filterState.trim().length > 0;
  };

  const isAllChecked = visibleOptions => {
    if (hasFilter()) {
      return props?.value?.length === visibleOptions?.length;
    } else {
      return props?.value?.length === props?.options?.length;
    }
  };

  const filterOptions = options => {
    let filterValue = filterState.trim().toLowerCase();
    let searchFields = filterBy ? filterBy.split(valuesSeparator) : [optionLabel || 'label'];
    return MultiSelectUtils.filter(options, searchFields, filterValue, filterMatchMode);
  };

  const getOptionLabel = option => {
    return optionLabel
      ? ObjectUtils.resolveFieldData(option, optionLabel)
      : option['label'] !== undefined
      ? option['label']
      : option;
  };

  const getOptionValue = option => {
    return optionValue
      ? ObjectUtils.resolveFieldData(option, optionValue)
      : option['value'] !== undefined
      ? option['value']
      : option;
  };

  const isEmpty = () => {
    return !value || value.length === 0;
  };

  const getSelectedItemsLabel = () => {
    let pattern = /{(.*?)}/;
    if (pattern.test(selectedItemsLabel)) {
      return selectedItemsLabel.replace(selectedItemsLabel.match(pattern)[0], value.length + '');
    }

    return selectedItemsLabel;
  };

  const getLabel = () => {
    if (isEmpty() || fixedPlaceholder) {
      return '';
    }

    if (hasSelectedItemsLabel) {
      const label = value
        .map(value => findLabelByValue(value))
        .filter(item => item !== null)
        .join(addSpaceAfterSeparator ? `${valuesSeparator} ` : valuesSeparator);

      if (label === '') {
        return [];
      }

      if (value.length <= maxSelectedLabels) {
        return label;
      } else {
        return getSelectedItemsLabel();
      }
    }
  };

  const getLabelContent = () => {
    if (selectedItemTemplate) {
      if (!isEmpty()) {
        if (value.length <= maxSelectedLabels) {
          return value.map(val => {
            return <Fragment key={val}>{selectedItemTemplate(val)}</Fragment>;
          });
        } else {
          return getSelectedItemsLabel();
        }
      } else {
        return selectedItemTemplate();
      }
    } else {
      return getLabel();
    }
  };

  const renderTooltip = () => {
    tooltip = new Tooltip({
      target: containerRef.current,
      content: tooltip,
      options: tooltipOptions
    });
  };

  const renderHeader = items => {
    return (
      <MultiSelectHeader
        allChecked={isAllChecked(items)}
        checkAllHeader={checkAllHeader}
        clearButton={clearButton}
        filter={filterState}
        filterPlaceholder={filterPlaceholder}
        filterValue={filterState}
        headerClassName={headerClassName}
        id={id}
        isPanelVisible={isPanelVisible}
        notCheckAllHeader={notCheckAllHeader}
        onClose={onCloseClick}
        onFilter={onFilter}
        onToggleAll={onToggleAll}
      />
    );
  };

  const renderLabel = () => {
    const empty = isEmpty();
    const content = getLabelContent();
    const className = classNames('p-multiselect-label', {
      'p-placeholder': empty && placeholder,
      'p-multiselect-label-empty': empty && !placeholder && !selectedItemTemplate
    });

    return hasSelectedItemsLabel ? (
      isLoadingData ? (
        <Spinner className={styles.spinner} />
      ) : (
        <div
          className="p-multiselect-label-container"
          style={{
            position: isFilter ? 'absolute' : 'relative',
            top: '0',
            paddingTop: '0.1rem',
            width: '100%'
          }}>
          <label className={className}>{content || placeholder || 'empty'}</label>
        </div>
      )
    ) : null;
  };

  let className = classNames('p-multiselect p-component', classNameProp, {
    'p-disabled': disabled
  });
  let label = renderLabel();
  let items = options;

  const renderMultiSelectItems = () => {
    if (items) {
      if (hasFilter()) {
        items = filterOptions(items);
      }

      return items.map(option => {
        let optionLabel = getOptionLabel(option);
        return (
          <MultiSelectItem
            disabled={option.disabled}
            key={optionLabel}
            label={optionLabel}
            onClick={onOptionClick}
            onKeyDown={onOptionKeyDown}
            option={option}
            selected={isSelected(option)}
            tabIndex={tabIndex}
            template={itemTemplate}
          />
        );
      });
    }
  };

  let header = renderHeader(items);
  let labelContent = getLabelContent();
  let multiselectitems = renderMultiSelectItems();

  return (
    <div className={className} id={id} onClick={onClick} ref={containerRef} style={style}>
      <div className={`p-hidden-accessible ${inputClassName}`}>
        <input
          aria-haspopup="listbox"
          aria-labelledby={ariaLabelledBy}
          className={labelContent ? 'p-filled' : ''}
          id={inputId}
          onBlur={onBlurFunction}
          onFocus={onFocusFunction}
          ref={focusInputRef}
          type="text"
        />
        <label htmlFor={inputId}>{labelProp}</label>
      </div>
      {label}
      <div className="p-multiselect-trigger">
        <span className="p-multiselect-trigger-icon pi pi-chevron-down p-c"></span>
      </div>
      <MultiSelectPanel
        appendTo={appendTo}
        header={header}
        label={labelProp}
        onClick={onPanelClick}
        panelClassName={panelClassName}
        ref={panelRef}
        scrollHeight={scrollHeight}>
        {multiselectitems}
      </MultiSelectPanel>
    </div>
  );
};

export default MultiSelectWebform;
