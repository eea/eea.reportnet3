import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

import { MultiSelectHeader } from './_components/MultiSelectHeader';
import { MultiSelectItem } from './_components/MultiSelectItem';
import { MultiSelectPanel } from './_components/MultiSelectPanel';
import { Spinner } from 'views/_components/Spinner';

import Tooltip from 'primereact/tooltip';

import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import MultiSelectUtils from './_functions/MultiSelectUtils';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

export class MultiSelect extends Component {
  static defaultProps = {
    addSpaceAfterSeparator: true,
    appendTo: null,
    ariaLabelledBy: null,
    checkAllHeader: null,
    className: null,
    clearButton: true,
    dataKey: null,
    disabled: false,
    filter: false,
    filterBy: null,
    filterMatchMode: 'contains',
    filterPlaceholder: null,
    fixedPlaceholder: false,
    hasSelectedItemsLabel: true,
    headerClassName: null,
    id: null,
    inputClassName: null,
    inputId: null,
    isFilter: false,
    isLoadingData: false,
    itemTemplate: null,
    label: null,
    maxSelectedLabels: 3,
    notCheckAllHeader: null,
    onBlur: null,
    onChange: null,
    onFilterInputChangeBackend: null,
    onFocus: null,
    optionLabel: null,
    optionValue: null,
    options: null,
    placeholder: null,
    scrollHeight: '200px',
    selectedItemTemplate: null,
    selectedItemsLabel: '{0} items selected',
    style: null,
    tabIndex: '0',
    tooltip: null,
    tooltipOptions: null,
    value: null,
    valuesSeparator: ','
  };

  static propTypes = {
    addSpaceAfterSeparator: PropTypes.bool,
    appendTo: PropTypes.object,
    ariaLabelledBy: PropTypes.string,
    checkAllHeader: PropTypes.string,
    className: PropTypes.string,
    clearButton: PropTypes.bool,
    dataKey: PropTypes.string,
    disabled: PropTypes.bool,
    filter: PropTypes.bool,
    filterBy: PropTypes.string,
    filterMatchMode: PropTypes.string,
    filterPlaceholder: PropTypes.string,
    fixedPlaceholder: PropTypes.bool,
    hasSelectedItemsLabel: PropTypes.bool,
    headerClassName: PropTypes.string,
    id: PropTypes.string,
    inputClassName: PropTypes.string,
    inputId: PropTypes.string,
    isFilter: PropTypes.bool,
    itemTemplate: PropTypes.func,
    label: PropTypes.string,
    isLoadingData: PropTypes.bool,
    maxSelectedLabels: PropTypes.number,
    notCheckAllHeader: PropTypes.string,
    onBlur: PropTypes.func,
    onChange: PropTypes.func,
    onFilterInputChangeBackend: PropTypes.func,
    onFocus: PropTypes.func,
    optionLabel: PropTypes.string,
    options: PropTypes.array,
    optionValue: PropTypes.string,
    placeholder: PropTypes.string,
    scrollHeight: PropTypes.string,
    selectedItemsLabel: PropTypes.string,
    selectedItemTemplate: PropTypes.func,
    style: PropTypes.object,
    tabIndex: PropTypes.string,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    value: PropTypes.any,
    valuesSeparator: PropTypes.string
  };

  constructor(props) {
    super(props);
    this.state = {
      filter: '',
      isPanelVisible: false
    };

    this.onClick = this.onClick.bind(this);
    this.onPanelClick = this.onPanelClick.bind(this);
    this.onOptionClick = this.onOptionClick.bind(this);
    this.onOptionKeyDown = this.onOptionKeyDown.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onFilter = this.onFilter.bind(this);
    this.onCloseClick = this.onCloseClick.bind(this);
    this.onToggleAll = this.onToggleAll.bind(this);
  }

  onOptionClick(event) {
    let optionValue = this.getOptionValue(event.option);
    let selectionIndex = this.findSelectionIndex(optionValue);
    let newValue;

    if (selectionIndex !== -1) newValue = this.props.value.filter((val, i) => i !== selectionIndex);
    else newValue = [...(this.props.value || []), optionValue];

    this.updateModel(event.originalEvent, newValue);
  }

  onOptionKeyDown(event) {
    let listItem = event.originalEvent.currentTarget;

    switch (event.originalEvent.which) {
      //down
      case 40:
        var nextItem = this.findNextItem(listItem);
        if (nextItem) {
          nextItem.focus();
        }

        event.originalEvent.preventDefault();
        break;

      //up
      case 38:
        var prevItem = this.findPrevItem(listItem);
        if (prevItem) {
          prevItem.focus();
        }

        event.originalEvent.preventDefault();
        break;

      //enter
      case 13:
        this.onOptionClick(event);
        event.originalEvent.preventDefault();
        break;

      default:
        break;
    }
  }

  findNextItem(item) {
    let nextItem = item.nextElementSibling;

    if (nextItem) return !DomHandler.hasClass(nextItem, 'p-multiselect-item') ? this.findNextItem(nextItem) : nextItem;
    else return null;
  }

  findPrevItem(item) {
    let prevItem = item.previousElementSibling;

    if (prevItem) return !DomHandler.hasClass(prevItem, 'p-multiselect-item') ? this.findPrevItem(prevItem) : prevItem;
    else return null;
  }

  onClick() {
    if (this.props.disabled) {
      return;
    }

    if (this.documentClickListener) {
      this.selfClick = true;
    }

    if (!this.panelClick) {
      if (this.panel.element.offsetParent) {
        this.hide();
      } else {
        this.focusInput.focus();
        this.show();
      }
    }
  }

  onToggleAll(event) {
    let newValue;

    if (event.checked) {
      newValue = [];
    } else {
      let options = this.hasFilter() ? this.filterOptions(this.props.options) : this.props.options;
      if (options) {
        newValue = [];
        for (let option of options) {
          newValue.push(this.getOptionValue(option));
        }
      }
    }

    this.updateModel(event.originalEvent, newValue);
  }

  updateModel(event, value) {
    if (this.props.onChange) {
      this.props.onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: value
        }
      });
    }
  }

  onFilter(event) {
    if (this.props.onFilterInputChangeBackend) {
      this.props.onFilterInputChangeBackend(event.query);
      this.setState({ filter: event.query });
    } else {
      this.setState({ filter: event.query });
    }
  }

  onPanelClick() {
    this.panelClick = true;
  }

  show() {
    if (this.props.options && this.props.options.length) {
      this.panel.element.style.zIndex = String(DomHandler.generateZIndex());
      this.panel.element.style.display = 'block';

      setTimeout(() => {
        DomHandler.addClass(this.panel.element, 'p-input-overlay-visible');
        DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
      }, 1);

      this.alignPanel();
      this.bindDocumentClickListener();
      this.setState({ isPanelVisible: true });
    }
  }

  hide() {
    DomHandler.addClass(this.panel.element, 'p-input-overlay-hidden');
    DomHandler.removeClass(this.panel.element, 'p-input-overlay-visible');
    this.unbindDocumentClickListener();
    this.clearClickState();

    setTimeout(() => {
      if (this.panel) {
        this.panel.element.style.display = 'none';
        DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
        this.clearFilter();
        this.setState({ isPanelVisible: false });
      }
    }, 150);
  }

  alignPanel() {
    if (this.props.appendTo) {
      this.panel.element.style.minWidth = DomHandler.getWidth(this.container) + 'px';
      DomHandler.absolutePosition(this.panel.element, this.container);
    } else {
      DomHandler.relativePosition(this.panel.element, this.container);
    }
  }

  onCloseClick(event) {
    this.hide();
    event.preventDefault();
    event.stopPropagation();
  }

  findSelectionIndex(value) {
    let index = -1;

    if (this.props.value) {
      for (let i = 0; i < this.props.value.length; i++) {
        if (ObjectUtils.equals(this.props.value[i], value, this.props.dataKey)) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

  isSelected(option) {
    return this.findSelectionIndex(this.getOptionValue(option)) !== -1;
  }

  findLabelByValue(val) {
    let label = null;
    if (this.props.options) {
      for (let i = 0; i < this.props.options.length; i++) {
        let option = this.props.options[i];
        let optionValue = this.getOptionValue(option);

        if (ObjectUtils.equals(optionValue, val)) {
          label = this.getOptionLabel(option);
          break;
        }
      }
    }

    return label;
  }

  onFocus(event) {
    DomHandler.addClass(this.container, 'p-focus');

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onBlur(event) {
    DomHandler.removeClass(this.container, 'p-focus');

    if (this.props.onBlur) {
      this.props.onBlur(event);
    }
  }

  bindDocumentClickListener() {
    if (!this.documentClickListener) {
      this.documentClickListener = this.onDocumentClick.bind(this);
      document.addEventListener('click', this.documentClickListener);
    }
  }

  unbindDocumentClickListener() {
    if (this.documentClickListener) {
      document.removeEventListener('click', this.documentClickListener);
      this.documentClickListener = null;
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);
      else this.renderTooltip();
    }
  }

  componentWillUnmount() {
    this.unbindDocumentClickListener();

    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  onDocumentClick() {
    if (!this.selfClick && !this.panelClick && this.panel.element.offsetParent) {
      this.hide();
    }

    this.clearClickState();
  }

  clearClickState() {
    this.selfClick = false;
    this.panelClick = false;
  }

  clearFilter() {
    if (this.props.onFilterInputChangeBackend) {
      this.props.onFilterInputChangeBackend('');
      this.setState({ filter: '' });
    } else {
      this.setState({ filter: '' });
    }
  }

  hasFilter() {
    return this.state.filter && this.state.filter.trim().length > 0;
  }

  isAllChecked(visibleOptions) {
    if (this.hasFilter()) {
      return this.props?.value?.length === visibleOptions?.length;
    } else {
      return this.props?.value?.length === this.props?.options?.length;
    }
  }

  filterOptions(options) {
    let filterValue = this.state.filter.trim().toLowerCase();
    let searchFields = this.props.filterBy
      ? this.props.filterBy.split(this.props.valuesSeparator)
      : [this.props.optionLabel || 'label'];
    return MultiSelectUtils.filter(options, searchFields, filterValue, this.props.filterMatchMode);
  }

  getOptionLabel(option) {
    return this.props.optionLabel
      ? ObjectUtils.resolveFieldData(option, this.props.optionLabel)
      : option['label'] !== undefined
      ? option['label']
      : option;
  }

  getOptionValue(option) {
    return this.props.optionValue
      ? ObjectUtils.resolveFieldData(option, this.props.optionValue)
      : option['value'] !== undefined
      ? option['value']
      : option;
  }

  isEmpty() {
    return !this.props.value || this.props.value.length === 0;
  }

  getSelectedItemsLabel() {
    let pattern = /{(.*?)}/;
    if (pattern.test(this.props.selectedItemsLabel)) {
      return this.props.selectedItemsLabel.replace(
        this.props.selectedItemsLabel.match(pattern)[0],
        this.props.value.length + ''
      );
    }

    return this.props.selectedItemsLabel;
  }

  getLabel() {
    let label;

    if (!this.isEmpty() && !this.props.fixedPlaceholder) {
      label = '';
      for (let i = 0; i < this.props.value.length; i++) {
        if (i !== 0) {
          label += this.props.addSpaceAfterSeparator ? `${this.props.valuesSeparator} ` : this.props.valuesSeparator;
        }
        label += this.findLabelByValue(this.props.value[i]);
      }

      if (this.props.hasSelectedItemsLabel) {
        if (this.props.value.length <= this.props.maxSelectedLabels) {
          return label;
        } else {
          return this.getSelectedItemsLabel();
        }
      }
    }

    return label;
  }

  getLabelContent() {
    if (this.props.selectedItemTemplate) {
      if (!this.isEmpty()) {
        if (this.props.value.length <= this.props.maxSelectedLabels) {
          return this.props.value.map(val => {
            return <Fragment key={val}>{this.props.selectedItemTemplate(val)}</Fragment>;
          });
        } else {
          return this.getSelectedItemsLabel();
        }
      } else {
        return this.props.selectedItemTemplate();
      }
    } else {
      return this.getLabel();
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.container,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  onFilterInputChange(event) {
    if (this.props.onFilterInputChangeBackend) {
      this.props.onFilterInputChangeBackend(event.target.value);
      this.setState({ filter: event.target.value });
    } else {
      this.setState({ filter: event.target.value });
    }
  }

  renderHeader(items) {
    return (
      <MultiSelectHeader
        allChecked={this.isAllChecked(items)}
        checkAllHeader={this.props.checkAllHeader}
        clearButton={this.props.clearButton}
        filter={this.props.filter}
        filterPlaceholder={this.props.filterPlaceholder}
        filterValue={this.state.filter}
        headerClassName={this.props.headerClassName}
        id={this.props.id}
        isPanelVisible={this.state.isPanelVisible}
        notCheckAllHeader={this.props.notCheckAllHeader}
        onClose={this.onCloseClick}
        onFilter={this.onFilter}
        onToggleAll={this.onToggleAll}
      />
    );
  }

  renderLabel() {
    const empty = this.isEmpty();
    const content = this.getLabelContent();
    const className = classNames('p-multiselect-label', {
      'p-placeholder': empty && this.props.placeholder,
      'p-multiselect-label-empty': empty && !this.props.placeholder && !this.props.selectedItemTemplate
    });

    return this.props.hasSelectedItemsLabel ? (
      this.props.isLoadingData ? (
        <Spinner style={{ top: 0, width: '25px', height: '25px', position: 'absolute' }} />
      ) : (
        <div
          className="p-multiselect-label-container"
          style={{
            position: this.props.isFilter ? 'absolute' : 'relative',
            top: '0',
            paddingTop: '0.1rem',
            width: '100%'
          }}>
          <label className={className}>{content || this.props.placeholder || 'empty'}</label>
        </div>
      )
    ) : null;
  }

  render() {
    let className = classNames('p-multiselect p-component', this.props.className, {
      'p-disabled': this.props.disabled
    });
    let label = this.renderLabel();
    let items = this.props.options;

    if (items) {
      if (this.hasFilter()) {
        items = this.filterOptions(items);
      }

      items = items.map(option => {
        let optionLabel = this.getOptionLabel(option);

        return (
          <MultiSelectItem
            disabled={option.disabled}
            key={optionLabel}
            label={optionLabel}
            onClick={this.onOptionClick}
            onKeyDown={this.onOptionKeyDown}
            option={option}
            selected={this.isSelected(option)}
            tabIndex={this.props.tabIndex}
            template={this.props.itemTemplate}
          />
        );
      });
    }

    let header = this.renderHeader(items);
    let labelContent = this.getLabelContent();

    return (
      <div
        className={className}
        id={this.props.id}
        onClick={this.onClick}
        ref={el => (this.container = el)}
        style={this.props.style}>
        <div className={`p-hidden-accessible ${this.props.inputClassName}`}>
          <input
            aria-haspopup="listbox"
            aria-labelledby={this.props.ariaLabelledBy}
            className={labelContent ? 'p-filled' : ''}
            id={this.props.inputId}
            onBlur={this.onBlur}
            onFocus={this.onFocus}
            ref={el => (this.focusInput = el)}
            type="text"
          />
          <label htmlFor={this.props.inputId}>{this.props.label}</label>
        </div>
        {label}
        <div className="p-multiselect-trigger">
          <span className="p-multiselect-trigger-icon pi pi-chevron-down p-c"></span>
        </div>
        <MultiSelectPanel
          appendTo={this.props.appendTo}
          header={header}
          label={this.props.label}
          onClick={this.onPanelClick}
          ref={el => (this.panel = el)}
          scrollHeight={this.props.scrollHeight}>
          {items}
        </MultiSelectPanel>
      </div>
    );
  }
}
