import { Component } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';

import './Dropdown.scss';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import FilterUtils from 'views/_functions/PrimeReact/FilterUtils';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import classNames from 'classnames';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { DropdownPanel } from './_components/DropdownPanel';
import { DropdownItem } from './_components/DropdownItem';
import { Spinner } from 'views/_components/Spinner';
import Tooltip from 'primereact/tooltip';

export class Dropdown extends Component {
  static defaultProps = {
    appendTo: null,
    ariaLabel: null,
    ariaLabelledBy: null,
    autoFocus: false,
    className: null,
    currentValue: undefined,
    dataKey: null,
    disabled: false,
    editable: false,
    filter: false,
    filterBy: null,
    filterInputAutoFocus: true,
    filterMatchMode: 'contains',
    filterPlaceholder: null,
    id: null,
    inputClassName: null,
    inputId: null,
    isLoadingData: false,
    itemTemplate: null,
    label: null,
    maxLength: null,
    name: null,
    onChange: null,
    onContextMenu: null,
    onEmptyList: null,
    onFilterInputChangeBackend: null,
    onKeyPress: null,
    onMouseDown: null,
    optionLabel: null,
    options: null,
    panelClassName: null,
    panelStyle: null,
    placeholder: null,
    required: false,
    scrollHeight: '200px',
    showClear: false,
    showFilterClear: false,
    style: null,
    tabIndex: null,
    tooltip: null,
    tooltipOptions: null,
    value: null
  };

  static propTypes = {
    appendTo: PropTypes.any,
    ariaLabel: PropTypes.string,
    ariaLabelledBy: PropTypes.string,
    autoFocus: PropTypes.bool,
    className: PropTypes.string,
    currentValue: PropTypes.string,
    dataKey: PropTypes.string,
    disabled: PropTypes.bool,
    editable: PropTypes.bool,
    filter: PropTypes.bool,
    filterBy: PropTypes.string,
    filterInputAutoFocus: PropTypes.bool,
    filterMatchMode: PropTypes.string,
    filterPlaceholder: PropTypes.string,
    id: PropTypes.string,
    inputClassName: PropTypes.string,
    inputId: PropTypes.string,
    isLoadingData: PropTypes.bool,
    itemTemplate: PropTypes.func,
    // eslint-disable-next-line react/no-unused-prop-types
    label: PropTypes.string,
    maxLength: PropTypes.number,
    name: PropTypes.string,
    onChange: PropTypes.func,
    onContextMenu: PropTypes.func,
    onEmptyList: PropTypes.func,
    onFilterInputChangeBackend: PropTypes.func,
    onKeyPress: PropTypes.func,
    onMouseDown: PropTypes.func,
    optionLabel: PropTypes.string,
    options: PropTypes.array,
    panelClassName: PropTypes.string,
    // eslint-disable-next-line react/no-unused-prop-types
    panelstyle: PropTypes.object,
    placeholder: PropTypes.string,
    required: PropTypes.bool,
    scrollHeight: PropTypes.string,
    showClear: PropTypes.bool,
    showFilterClear: PropTypes.bool,
    style: PropTypes.object,
    tabIndex: PropTypes.number,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    value: PropTypes.any
  };

  constructor(props) {
    super(props);
    this.state = {
      filter: this.props.currentValue ? this.props.currentValue : ''
    };

    this.onClick = this.onClick.bind(this);
    this.onInputFocus = this.onInputFocus.bind(this);
    this.onInputBlur = this.onInputBlur.bind(this);
    this.onInputKeyDown = this.onInputKeyDown.bind(this);
    this.onEditableInputClick = this.onEditableInputClick.bind(this);
    this.onEditableInputChange = this.onEditableInputChange.bind(this);
    this.onEditableInputFocus = this.onEditableInputFocus.bind(this);
    this.onOptionClick = this.onOptionClick.bind(this);
    this.onFilterInputChange = this.onFilterInputChange.bind(this);
    this.onFilterInputKeyDown = this.onFilterInputKeyDown.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.panelClick = this.panelClick.bind(this);
    this.clear = this.clear.bind(this);
    this.clearFilter = this.clearFilter.bind(this);
  }

  onKeyPress(event) {
    if (this.props.onKeyPress && event.which === 13) {
      this.props.onKeyPress(event);
    }
  }

  onClick(event) {
    if (this.props.disabled) {
      return;
    }

    if (this.documentClickListener) {
      this.selfClick = true;
    }

    let clearClick =
      DomHandler.hasClass(event.target, 'p-dropdown-clear-icon') ||
      DomHandler.hasClass(event.target, 'p-dropdown-clear-filter-icon');
    if (!this.overlayClick && !this.editableInputClick && !clearClick) {
      this.focusInput.focus();

      if (this.panel.element.offsetParent) {
        this.hide();
      } else {
        this.show();

        if (this.props.filter && this.props.filterInputAutoFocus) {
          setTimeout(() => {
            this.filterInput.focus();
          }, 200);
        }
      }
    }

    if (this.editableInputClick) {
      this.expeditableInputClick = false;
    }
  }

  panelClick() {
    this.overlayClick = true;
  }

  onInputFocus(event) {
    DomHandler.addClass(this.container, 'p-focus');
  }

  onInputBlur(event) {
    DomHandler.removeClass(this.container, 'p-focus');
  }

  onUpKey(event) {
    if (this.props.options) {
      let selectedItemIndex = this.findOptionIndex(this.props.value);
      let prevItem = this.findPrevVisibleItem(selectedItemIndex);

      if (prevItem) {
        this.selectItem({
          originalEvent: event,
          option: prevItem
        });
      }
    }

    event.preventDefault();
  }

  onDownKey(event) {
    if (this.props.options) {
      if (!this.panel.element.offsetParent && event.altKey) {
        this.show();
      } else {
        let selectedItemIndex = this.findOptionIndex(this.props.value);
        let nextItem = this.findNextVisibleItem(selectedItemIndex);

        if (nextItem) {
          this.selectItem({
            originalEvent: event,
            option: nextItem
          });
        }
      }
    }

    event.preventDefault();
  }

  onInputKeyDown(event) {
    switch (event.which) {
      //down
      case 40:
        this.onDownKey(event);
        break;

      //up
      case 38:
        this.onUpKey(event);
        break;

      //space
      case 32:
        if (!this.panel.element.offsetParent) {
          this.show();
          event.preventDefault();
        }
        break;

      //enter
      case 13:
        this.hide();
        this.onKeyPress(event);
        event.preventDefault();
        break;

      //escape and tab
      case 27:
      case 9:
        this.hide();
        break;

      default:
        this.search(event);
        break;
    }
  }

  search(event) {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    const char = String.fromCharCode(event.keyCode);
    this.previousSearchChar = this.currentSearchChar;
    this.currentSearchChar = char;

    if (this.previousSearchChar === this.currentSearchChar) this.searchValue = this.currentSearchChar;
    else this.searchValue = this.searchValue ? this.searchValue + char : char;

    let searchIndex = this.props.value ? this.findOptionIndex(this.props.value) : -1;
    let newOption = this.searchOption(++searchIndex);

    if (newOption) {
      this.selectItem({
        originalEvent: event,
        option: newOption
      });
      this.selectedOptionUpdated = true;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchValue = null;
    }, 250);
  }

  searchOption(index) {
    let option;

    if (this.searchValue) {
      option = this.searchOptionInRange(index, this.props.options.length);

      if (!option) {
        option = this.searchOptionInRange(0, index);
      }
    }

    return option;
  }

  searchOptionInRange(start, end) {
    for (let i = start; i < end; i++) {
      let opt = this.props.options[i];
      let label = this.getOptionLabel(opt).toString().toLowerCase();
      if (label.startsWith(this.searchValue.toLowerCase())) {
        return opt;
      }
    }

    return null;
  }

  findNextVisibleItem(index) {
    let i = index + 1;
    if (i === this.props.options.length) {
      return null;
    }

    let option = this.props.options[i];

    if (option.disabled) {
      return this.findNextVisibleItem(i);
    }

    if (this.hasFilter()) {
      if (this.filter(option)) return option;
      else return this.findNextVisibleItem(i);
    } else {
      return option;
    }
  }

  findPrevVisibleItem(index) {
    let i = index - 1;
    if (i === -1) {
      return null;
    }

    let option = this.props.options[i];

    if (option.disabled) {
      return this.findPrevVisibleItem(i);
    }

    if (this.hasFilter()) {
      if (this.filter(option)) return option;
      else return this.findPrevVisibleItem(i);
    } else {
      return option;
    }
  }

  onEditableInputClick(event) {
    this.editableInputClick = true;
    this.bindDocumentClickListener();
  }

  onEditableInputChange(event) {
    this.props.onChange({
      originalEvent: event.originalEvent,
      value: event.target.value,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: this.props.name,
        id: this.props.id,
        value: event.target.value
      }
    });
  }

  onEditableInputFocus(event) {
    DomHandler.addClass(this.container, 'p-focus');
    this.hide();
  }

  onOptionClick(event) {
    const option = event.option;

    if (!option.disabled) {
      this.selectItem(event);
      this.focusInput.focus();
    }

    setTimeout(() => {
      this.hide();
    }, 100);
  }

  onFilterInputChange(event) {
    if (this.props.onFilterInputChangeBackend) {
      this.props.onFilterInputChangeBackend(event.target.value);
      this.setState({ filter: event.target.value });
    } else {
      this.setState({ filter: event.target.value });
    }
  }

  onFilterInputKeyDown(event) {
    switch (event.which) {
      //down
      case 40:
        this.onDownKey(event);
        break;

      //up
      case 38:
        this.onUpKey(event);
        break;

      //enter
      case 13:
        this.hide();
        event.preventDefault();
        break;

      default:
        break;
    }
  }

  clear(event) {
    this.props.onChange({
      originalEvent: event,
      value: null,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: this.props.name,
        id: this.props.id,
        value: null
      }
    });
    this.updateEditableLabel();
  }

  clearFilter() {
    if (this.props.onFilterInputChangeBackend) {
      this.props.onFilterInputChangeBackend('');
      this.setState({ filter: '' });
      setTimeout(() => {
        this.filterInput.focus();
      }, 200);
    } else {
      this.setState({ filter: '' });
      this.filterInput.focus();
    }
  }

  selectItem(event) {
    let currentSelectedOption = this.findOption(this.props.value);
    if (currentSelectedOption !== event.option) {
      this.updateEditableLabel(event.option);
      this.props.onChange({
        originalEvent: event.originalEvent,
        value: this.props.optionLabel ? event.option : event.option.value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: this.props.optionLabel ? event.option : event.option.value
        }
      });
    }
  }

  findOptionIndex(value) {
    let index = -1;
    if (this.props.options) {
      for (let i = 0; i < this.props.options.length; i++) {
        let optionValue = this.props.optionLabel ? this.props.options[i] : this.props.options[i].value;
        if ((value === null && optionValue == null) || ObjectUtils.equals(value, optionValue, this.props.dataKey)) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

  findOption(value) {
    let index = this.findOptionIndex(value);
    return index !== -1 ? this.props.options[index] : null;
  }

  show() {
    this.panel.element.style.zIndex = String(DomHandler.generateZIndex());
    this.panel.element.style.display = 'block';

    setTimeout(() => {
      DomHandler.addClass(this.panel.element, 'p-input-overlay-visible');
      DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
    }, 1);

    this.alignPanel();
    this.bindDocumentClickListener();
  }

  hide() {
    if (this.panel && this.panel.element) {
      DomHandler.addClass(this.panel.element, 'p-input-overlay-hidden');
      DomHandler.removeClass(this.panel.element, 'p-input-overlay-visible');
      this.unbindDocumentClickListener();
      this.clearClickState();

      this.hideTimeout = setTimeout(() => {
        this.panel.element.style.display = 'none';
        DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
      }, 150);
    }
  }

  alignPanel() {
    if (this.props.appendTo) {
      this.panel.element.style.minWidth = DomHandler.getWidth(this.container) + 'px';
      DomHandler.absolutePosition(this.panel.element, this.container);
    } else {
      DomHandler.relativePosition(this.panel.element, this.container);
    }
  }

  bindDocumentClickListener() {
    if (!this.documentClickListener) {
      this.documentClickListener = () => {
        if (!this.selfClick && !this.overlayClick) {
          this.hide();
        }

        this.clearClickState();
      };

      document.addEventListener('click', this.documentClickListener);
    }
  }

  unbindDocumentClickListener() {
    if (this.documentClickListener) {
      document.removeEventListener('click', this.documentClickListener);
      this.documentClickListener = null;
    }
  }

  clearClickState() {
    this.selfClick = false;
    this.editableInputClick = false;
    this.overlayClick = false;
  }

  updateEditableLabel(option) {
    if (this.editableInput) {
      this.editableInput.value = option ? this.getOptionLabel(option) : this.props.value || '';
    }
  }

  filter(options) {
    let filterValue = this.state.filter.trim().toLocaleLowerCase(this.props.filterLocale);
    let searchFields = this.props.filterBy ? this.props.filterBy.split(',') : [this.props.optionLabel || 'label'];
    let items = FilterUtils.filter(
      options,
      searchFields,
      filterValue,
      this.props.filterMatchMode,
      this.props.filterLocale
    );

    return items && items.length ? items : null;
  }

  hasFilter() {
    return this.state.filter && this.state.filter.trim().length > 0;
  }

  renderHiddenSelect(selectedOption) {
    let placeHolderOption = <option value="">{this.props.placeholder}</option>;
    let option = selectedOption ? (
      <option value={selectedOption.value}>{this.getOptionLabel(selectedOption)}</option>
    ) : null;

    return (
      <div className="p-hidden-accessible p-dropdown-hidden-select">
        <select
          aria-hidden="true"
          aria-label={this.props.name}
          id={this.props.name}
          name={this.props.name}
          ref={el => (this.nativeSelect = el)}
          required={this.props.required}
          tabIndex="-1">
          {placeHolderOption}
          {option}
        </select>
        <label className="srOnly" htmlFor={this.props.name}>
          {this.props.name}
        </label>
      </div>
    );
  }

  renderKeyboardHelper(label) {
    return (
      <div className={`p-hidden-accessible ${this.props.inputClassName}`}>
        <input
          aria-label={this.props.ariaLabel}
          aria-labelledby={this.props.ariaLabelledBy}
          className={label ? 'p-filled' : ''}
          disabled={this.props.disabled}
          id={this.props.inputId}
          onBlur={this.onInputBlur}
          onFocus={this.onInputFocus}
          onKeyDown={this.onInputKeyDown}
          readOnly={true}
          ref={el => (this.focusInput = el)}
          role="listbox"
          tabIndex={this.props.tabIndex}
          type="text"
        />
        <label htmlFor={this.props.inputId}>{this.props.label}</label>
      </div>
    );
  }

  renderLabel(label, selectedOption) {
    if (this.props.editable) {
      let value = label || this.props.value || '';

      return (
        <input
          aria-label={this.props.ariaLabel}
          aria-labelledby={this.props.ariaLabelledBy}
          className="p-dropdown-label p-inputtext"
          defaultValue={value}
          disabled={this.props.disabled}
          maxLength={this.props.maxLength}
          onBlur={this.onInputBlur}
          onClick={this.onEditableInputClick}
          onFocus={this.onEditableInputFocus}
          onInput={this.onEditableInputChange}
          placeholder={this.props.placeholder}
          ref={el => (this.editableInput = el)}
          type="text"
        />
      );
    } else {
      let className = classNames('p-dropdown-label p-inputtext', {
        'p-placeholder': label === null && this.props.placeholder,
        'p-dropdown-label-empty': label === null && !this.props.placeholder
      });

      return (
        <label className={className} style={{ fontStyle: isNull(selectedOption) ? 'italic' : 'inherit' }}>
          {label || `${this.props.placeholder}` || 'empty'}{' '}
          {this.props.required && isNull(selectedOption) ? (
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
  }

  renderClearIcon() {
    if (this.props.value != null && this.props.showClear && !this.props.disabled) {
      return <i className="p-dropdown-clear-icon pi pi-times" onClick={this.clear}></i>;
    } else {
      return null;
    }
  }

  renderDropdownIcon() {
    return (
      <div className="p-dropdown-trigger">
        <span className="p-dropdown-trigger-icon pi pi-chevron-down p-clickable"></span>
      </div>
    );
  }

  renderItems(selectedOption) {
    let items = this.props.options;

    if (items && this.hasFilter()) {
      items = this.filter(items);
    }

    if (items) {
      return items.map(option => {
        let optionLabel = this.getOptionLabel(option);
        return (
          <DropdownItem
            disabled={option.disabled}
            key={this.getOptionKey(option)}
            label={optionLabel}
            onClick={this.onOptionClick}
            option={option}
            selected={selectedOption === option}
            template={this.props.itemTemplate}
          />
        );
      });
    } else {
      if (this.props.onEmptyList) {
        this.props.onEmptyList();
      }
      return null;
    }
  }

  renderFilter() {
    if (this.props.filter) {
      return (
        <div className="p-dropdown-filter-container">
          <input
            aria-label={this.props.ariaLabel}
            aria-labelledby={this.props.ariaLabelledBy}
            autoComplete="off"
            className="p-dropdown-filter p-inputtext p-component"
            onChange={this.onFilterInputChange}
            onKeyDown={this.onFilterInputKeyDown}
            placeholder={this.props.filterPlaceholder}
            ref={el => (this.filterInput = el)}
            type="text"
            value={this.state.filter}
          />
          {this.props.showFilterClear && !isEmpty(this.state.filter) ? (
            <span className="p-dropdown-filter-clear-icon pi pi-times" onClick={this.clearFilter}></span>
          ) : null}
          <span className="p-dropdown-filter-icon pi pi-search"></span>
        </div>
      );
    } else {
      return null;
    }
  }

  getOptionLabel(option) {
    return this.props.optionLabel ? ObjectUtils.resolveFieldData(option, this.props.optionLabel) : option.label;
  }

  getOptionKey(option) {
    return this.props.dataKey ? ObjectUtils.resolveFieldData(option, this.props.dataKey) : this.getOptionLabel(option);
  }

  checkValidity() {
    return this.nativeSelect.checkValidity;
  }

  componentDidMount() {
    if (this.props.autoFocus && this.focusInput) {
      this.focusInput.focus();
    }

    if (this.props.tooltip) {
      this.renderTooltip();
    }

    this.nativeSelect.selectedIndex = 1;
  }

  componentWillUnmount() {
    this.unbindDocumentClickListener();

    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }

    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.props.filter) {
      this.alignPanel();
    }

    if (this.panel.element.offsetParent) {
      let highlightItem = DomHandler.findSingle(this.panel.element, 'li.p-highlight');
      if (highlightItem) {
        DomHandler.scrollInView(this.panel.itemsWrapper, highlightItem);
      }
    }

    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);
      else this.renderTooltip();
    }

    this.nativeSelect.selectedIndex = 1;
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.container,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    let className = classNames('p-dropdown p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-dropdown-clearable': this.props.showClear && !this.props.disabled
    });
    let selectedOption = this.findOption(this.props.value);
    let label = selectedOption ? this.getOptionLabel(selectedOption) : null;

    let hiddenSelect = this.renderHiddenSelect(selectedOption);
    let keyboardHelper = this.renderKeyboardHelper(label);
    let labelElement = this.props.isLoadingData ? (
      <Spinner style={{ top: 0, width: '25px', height: '25px' }} />
    ) : (
      this.renderLabel(label, selectedOption)
    );
    let dropdownIcon = this.renderDropdownIcon();
    let items = this.renderItems(selectedOption);
    let filterElement = this.renderFilter();
    let clearIcon = this.renderClearIcon();

    if (this.props.editable && this.editableInput) {
      let value = label || this.props.value || '';
      this.editableInput.value = value;
    }

    return (
      <div
        className={className}
        id={this.props.id}
        onClick={this.onClick}
        onContextMenu={this.props.onContextMenu}
        onKeyPress={this.props.onKeyPress}
        onMouseDown={this.props.onMouseDown}
        ref={el => (this.container = el)}
        style={this.props.style}>
        {keyboardHelper}
        {hiddenSelect}
        {labelElement}
        {clearIcon}
        {dropdownIcon}
        <DropdownPanel
          appendTo={this.props.appendTo}
          filter={filterElement}
          onClick={this.panelClick}
          panelClassName={this.props.panelClassName}
          panelStyle={this.props.panelStyle}
          ref={el => (this.panel = el)}
          scrollHeight={this.props.scrollHeight}>
          {items}
        </DropdownPanel>
      </div>
    );
  }
}
