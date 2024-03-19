import { Fragment } from 'react';

import classNames from 'classnames';

import styles from './MultiSelect.module.scss';

import { MultiSelectHeader } from './_components/MultiSelectHeader';
import { MultiSelectItem } from './_components/MultiSelectItem';
import MultiSelectPanel from './_components/MultiSelectPanel/MultiSelectPanel';
import { Spinner } from 'views/_components/Spinner';

import Tooltip from 'primereact/tooltip';

import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import MultiSelectUtils from './_functions/MultiSelectUtils';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

const MultiSelect = props => {
  const onOptionClick = event => {
    let optionValue = getOptionValue(event.option);
    let selectionIndex = findSelectionIndex(optionValue);
    let newValue;

    if (selectionIndex !== -1) newValue = props.value.filter((val, i) => i !== selectionIndex);
    else newValue = [...(props.value || []), optionValue];

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
    if (props.disabled) {
      return;
    }

    if (documentClickListener) {
      selfClick = true;
    }

    if (!panelClick) {
      if (panel.element.offsetParent) {
        hide();
      } else {
        focusInput.focus();
        show();
      }
    }
  };

  const onToggleAll = event => {
    let newValue;

    if (event.checked) {
      newValue = [];
    } else {
      let options = hasFilter() ? filterOptions(props.options) : props.options;
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
    if (props.onChange) {
      props.onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: props.name,
          id: props.id,
          value: value
        }
      });
    }
  };

  const onFilter = event => {
    if (props.onFilterInputChangeBackend) {
      props.onFilterInputChangeBackend(event.query);
      setState({ filter: event.query });
    } else {
      setState({ filter: event.query });
    }
  };

  const onPanelClick = () => {
    panelClick = true;
  };

  const show = () => {
    if (props.options && props.options.length) {
      panel.element.style.zIndex = String(DomHandler.generateZIndex());
      panel.element.style.display = 'block';

      setTimeout(() => {
        DomHandler.addClass(panel.element, 'p-input-overlay-visible');
        DomHandler.removeClass(panel.element, 'p-input-overlay-hidden');
      }, 1);

      alignPanel();
      bindDocumentClickListener();
      setState({ isPanelVisible: true });
    }
  };

  const hide = () => {
    DomHandler.addClass(panel.element, 'p-input-overlay-hidden');
    DomHandler.removeClass(panel.element, 'p-input-overlay-visible');
    unbindDocumentClickListener();
    clearClickState();

    setTimeout(() => {
      if (panel) {
        panel.element.style.display = 'none';
        DomHandler.removeClass(panel.element, 'p-input-overlay-hidden');
        clearFilter();
        setState({ isPanelVisible: false });
      }
    }, 150);
  };

  const alignPanel = () => {
    if (props.appendTo) {
      panel.element.style.minWidth = DomHandler.getWidth(container) + 'px';
      DomHandler.absolutePosition(panel.element, container);
    } else {
      DomHandler.relativePosition(panel.element, container);
    }
  };

  const onCloseClick = event => {
    hide();
    event.preventDefault();
    event.stopPropagation();
  };

  const findSelectionIndex = value => {
    let index = -1;

    if (props.value) {
      for (let i = 0; i < props.value.length; i++) {
        if (ObjectUtils.equals(props.value[i], value, props.dataKey)) {
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
    if (props.options) {
      for (let i = 0; i < props.options.length; i++) {
        let option = props.options[i];
        let optionValue = getOptionValue(option);

        if (ObjectUtils.equals(optionValue, val)) {
          label = getOptionLabel(option);
          break;
        }
      }
    }

    return label;
  };

  const onFocus = event => {
    DomHandler.addClass(container, 'p-focus');

    if (props.onFocus) {
      props.onFocus(event);
    }
  };

  const onBlur = event => {
    DomHandler.removeClass(container, 'p-focus');

    if (props.onBlur) {
      props.onBlur(event);
    }
  };

  const bindDocumentClickListener = () => {
    if (!documentClickListener) {
      documentClickListener = onDocumentClick.bind(this);
      document.addEventListener('click', documentClickListener);
    }
  };

  const unbindDocumentClickListener = () => {
    if (documentClickListener) {
      document.removeEventListener('click', documentClickListener);
      documentClickListener = null;
    }
  };

  const componentDidMount = () => {
    if (props.tooltip) {
      renderTooltip();
    }
  };

  const componentDidUpdate = prevProps => {
    if (prevProps.tooltip !== props.tooltip) {
      if (tooltip) tooltip.updateContent(props.tooltip);
      else renderTooltip();
    }
  };

  const componentWillUnmount = () => {
    unbindDocumentClickListener();

    if (tooltip) {
      tooltip.destroy();
      tooltip = null;
    }
  };

  const onDocumentClick = () => {
    if (!selfClick && !panelClick && panel.element.offsetParent) {
      hide();
    }

    clearClickState();
  };

  const clearClickState = () => {
    selfClick = false;
    panelClick = false;
  };

  const clearFilter = () => {
    if (props.onFilterInputChangeBackend) {
      props.onFilterInputChangeBackend('');
      setState({ filter: '' });
    } else {
      setState({ filter: '' });
    }
  };

  const hasFilter = () => {
    return filter && filter.trim().length > 0;
  };

  const isAllChecked = visibleOptions => {
    if (hasFilter()) {
      return props?.value?.length === visibleOptions?.length;
    } else {
      return props?.value?.length === props?.options?.length;
    }
  };

  const filterOptions = options => {
    let filterValue = state.filter.trim().toLowerCase();
    let searchFields = props.filterBy ? props.filterBy.split(props.valuesSeparator) : [props.optionLabel || 'label'];
    return MultiSelectUtils.filter(options, searchFields, filterValue, props.filterMatchMode);
  };

  const getOptionLabel = option => {
    return props.optionLabel
      ? ObjectUtils.resolveFieldData(option, props.optionLabel)
      : option['label'] !== undefined
      ? option['label']
      : option;
  };

  const getOptionValue = option => {
    return props.optionValue
      ? ObjectUtils.resolveFieldData(option, props.optionValue)
      : option['value'] !== undefined
      ? option['value']
      : option;
  };

  const isEmpty = () => {
    return !props.value || props.value.length === 0;
  };

  const getSelectedItemsLabel = () => {
    let pattern = /{(.*?)}/;
    if (pattern.test(props.selectedItemsLabel)) {
      return props.selectedItemsLabel.replace(props.selectedItemsLabel.match(pattern)[0], props.value.length + '');
    }

    return props.selectedItemsLabel;
  };

  const getLabel = () => {
    if (isEmpty() || props.fixedPlaceholder) {
      return '';
    }

    if (props.hasSelectedItemsLabel) {
      const label = props.value
        .map(value => findLabelByValue(value))
        .filter(item => item !== null)
        .join(props.addSpaceAfterSeparator ? `${props.valuesSeparator} ` : props.valuesSeparator);

      if (label === '') {
        return [];
      }

      if (props.value.length <= props.maxSelectedLabels) {
        return label;
      } else {
        return getSelectedItemsLabel();
      }
    }
  };

  const getLabelContent = () => {
    if (props.selectedItemTemplate) {
      if (!isEmpty()) {
        if (props.value.length <= props.maxSelectedLabels) {
          return props.value.map(val => {
            return <Fragment key={val}>{props.selectedItemTemplate(val)}</Fragment>;
          });
        } else {
          return getSelectedItemsLabel();
        }
      } else {
        return props.selectedItemTemplate();
      }
    } else {
      return getLabel();
    }
  };

  const renderTooltip = () => {
    tooltip = new Tooltip({
      target: container,
      content: props.tooltip,
      options: props.tooltipOptions
    });
  };

  const onFilterInputChange = event => {
    if (props.onFilterInputChangeBackend) {
      props.onFilterInputChangeBackend(event.target.value);
      setState({ filter: event.target.value });
    } else {
      setState({ filter: event.target.value });
    }
  };

  const renderHeader = items => {
    return (
      <MultiSelectHeader
        allChecked={isAllChecked(items)}
        checkAllHeader={props.checkAllHeader}
        clearButton={props.clearButton}
        filter={props.filter}
        filterPlaceholder={props.filterPlaceholder}
        filterValue={state.filter}
        headerClassName={props.headerClassName}
        id={props.id}
        isPanelVisible={state.isPanelVisible}
        notCheckAllHeader={props.notCheckAllHeader}
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
      'p-placeholder': empty && props.placeholder,
      'p-multiselect-label-empty': empty && !props.placeholder && !props.selectedItemTemplate
    });

    return props.hasSelectedItemsLabel ? (
      props.isLoadingData ? (
        <Spinner className={styles.spinner} />
      ) : (
        <div
          className="p-multiselect-label-container"
          style={{
            position: props.isFilter ? 'absolute' : 'relative',
            top: '0',
            paddingTop: '0.1rem',
            width: '100%'
          }}>
          <label className={className}>{content || props.placeholder || 'empty'}</label>
        </div>
      )
    ) : null;
  };

  const className = classNames('p-multiselect p-component', propClassName, {
    'p-disabled': disabled
  });

  const header = renderHeader(items);
  const labelContent = getLabelContent();

  let items = propOptions;

  if (items) {
    if (hasFilter()) {
      items = filterOptions(items);
    }

    items = items.map(option => {
      const optionLabel = getOptionLabel(option);

      return (
        <MultiSelectItem
          disabled={option.disabled}
          key={optionLabel}
          label={optionLabel}
          onClick={() => onOptionClick(option)}
          onKeyDown={() => onOptionKeyDown(option)}
          option={option}
          selected={isSelected(option)}
          tabIndex={tabIndex}
          template={itemTemplate}
        />
      );
    });
  }

  return (
    <MultiSelectPanel
      appendTo={this.props.appendTo}
      header={header}
      label={this.props.label}
      onClick={this.onPanelClick}
      panelClassName={this.props.panelClassName}
      ref={el => (this.panel = el)}
      scrollHeight={this.props.scrollHeight}>
      {items}
    </MultiSelectPanel>
  );
};

export default MultiSelect;
