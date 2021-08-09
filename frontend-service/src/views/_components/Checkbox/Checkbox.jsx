import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Tooltip from 'primereact/tooltip';

import uniqueId from 'lodash/uniqueId';

export class Checkbox extends Component {
  static defaultProps = {
    ariaLabel: null,
    ariaLabelledBy: null,
    checked: false,
    className: null,
    disabled: false,
    id: null,
    inputId: null,
    name: null,
    onChange: null,
    onContextMenu: null,
    onMouseDown: null,
    readOnly: false,
    required: false,
    style: null,
    tooltip: null,
    tooltipOptions: null,
    value: null
  };

  static propTypes = {
    ariaLabel: PropTypes.string,
    ariaLabelledBy: PropTypes.string,
    checked: PropTypes.bool,
    className: PropTypes.string,
    disabled: PropTypes.bool,
    id: PropTypes.string,
    inputId: PropTypes.string,
    name: PropTypes.string,
    onChange: PropTypes.func,
    onContextMenu: PropTypes.func,
    onMouseDown: PropTypes.func,
    readOnly: PropTypes.bool,
    required: PropTypes.bool,
    style: PropTypes.object,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    value: PropTypes.any
  };

  constructor(props) {
    super(props);
    this.state = {};

    this.onClick = this.onClick.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  onClick(e) {
    if (!this.props.disabled && !this.props.readOnly && this.props.onChange) {
      this.props.onChange({
        originalEvent: e,
        value: this.props.value,
        checked: !this.props.checked,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          type: 'checkbox',
          name: this.props.name,
          id: this.props.id,
          value: this.props.value,
          checked: !this.props.checked
        }
      });

      this.input.checked = !this.props.checked;
      if (!this.props.checked) {
        this.input.focus();
      }

      e.preventDefault();
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  componentDidUpdate(prevProps) {
    this.input.checked = this.props.checked;

    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);
      else this.renderTooltip();
    }
  }

  onFocus() {
    this.setState({ focused: true });
  }

  onBlur() {
    this.setState({ focused: false });
  }

  onKeyDown(event) {
    if (event.key === 'Enter') {
      this.onClick(event);
      event.preventDefault();
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    let containerClass = classNames('p-checkbox p-component', this.props.className);
    let boxClass = classNames('p-checkbox-box p-component', {
      'p-highlight': this.props.checked,
      'p-disabled': this.props.disabled,
      'p-focus': this.state.focused
    });
    let iconClass = classNames('p-checkbox-icon p-c', { 'pi pi-check': this.props.checked });

    const id = uniqueId();

    return (
      <div
        className={containerClass}
        id={this.props.id}
        onClick={this.onClick}
        onContextMenu={this.props.onContextMenu}
        onMouseDown={this.props.onMouseDown}
        ref={el => (this.element = el)}
        style={this.props.style}>
        <div className="p-hidden-accessible">
          <input
            aria-label={this.props.ariaLabel}
            aria-labelledby={this.props.ariaLabelledBy}
            defaultChecked={this.props.checked}
            disabled={this.props.disabled}
            id={`${this.props.inputId}_${id}`}
            name={this.props.name}
            onBlur={this.onBlur}
            onFocus={this.onFocus}
            onKeyDown={this.onKeyDown}
            readOnly={this.props.readOnly}
            ref={el => (this.input = el)}
            required={this.props.required}
            type="checkbox"
          />
        </div>
        <div
          aria-checked={this.props.checked}
          aria-label={this.props.ariaLabel}
          aria-labelledby={this.props.ariaLabelledBy}
          className={boxClass}
          ref={el => (this.box = el)}
          role="checkbox">
          <span className={iconClass}></span>
        </div>
      </div>
    );
  }
}
