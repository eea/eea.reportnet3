import { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Tooltip from 'primereact/tooltip';

import './InputSwitch.scss';
import styles from './InputSwitch.module.scss';

import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';

export class InputSwitch extends Component {
  static defaultProps = {
    ariaLabelledBy: null,
    checked: false,
    className: null,
    disabled: false,
    id: null,
    inputId: null,
    name: null,
    onBlur: null,
    onChange: null,
    onFocus: null,
    sliderCheckedClassName: null,
    style: null,
    tooltip: null,
    tooltipOptions: null
  };

  static propTypes = {
    ariaLabelledBy: PropTypes.string,
    checked: PropTypes.bool,
    className: PropTypes.string,
    disabled: PropTypes.bool,
    id: PropTypes.string,
    inputId: PropTypes.string,
    name: PropTypes.string,
    onBlur: PropTypes.func,
    onChange: PropTypes.func,
    onFocus: PropTypes.func,
    style: PropTypes.object,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    sliderCheckedClassName: PropTypes.string
  };

  constructor(props) {
    super(props);
    this.state = {};
    this.onClick = this.onClick.bind(this);
    this.toggle = this.toggle.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  onClick(event) {
    if (this.props.disabled) {
      return;
    }

    this.toggle(event);
    this.input.focus();
  }

  toggle(event) {
    if (this.props.onChange) {
      this.props.onChange({
        originalEvent: event,
        value: !this.props.checked,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: !this.props.checked
        }
      });
    }
  }

  onFocus(event) {
    this.setState({ focused: true });

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onBlur(event) {
    this.setState({ focused: false });

    if (this.props.onBlur) {
      this.props.onBlur(event);
    }
  }

  onKeyDown(event) {
    if (event.key === 'Enter') {
      this.onClick(event);
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
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.container,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    const className = classNames('p-inputswitch p-component', this.props.className, {
      'p-inputswitch-checked': this.props.checked,
      'p-disabled': this.props.disabled,
      'p-inputswitch-focus': this.state.focused
    });

    let inputSwitchProps = ObjectUtils.findDiffKeys(this.props, InputSwitch.defaultProps);

    return (
      <div
        aria-checked={this.props.checked}
        className={className}
        id={this.props.id}
        onClick={this.onClick}
        ref={el => (this.container = el)}
        role="checkbox"
        style={this.props.style}
        {...inputSwitchProps}>
        <div className="p-hidden-accessible">
          <input
            aria-checked={this.props.checked}
            aria-labelledby={this.props.ariaLabelledBy}
            checked={this.props.checked}
            disabled={this.props.disabled}
            id={this.props.inputId}
            name={this.props.name}
            onBlur={this.onBlur}
            onChange={this.toggle}
            onFocus={this.onFocus}
            onKeyDown={this.onKeyDown}
            ref={el => (this.input = el)}
            role="switch"
            title={this.props.tooltip ? this.props.tooltip : 'switch'}
            type="checkbox"
          />
        </div>
        <span
          className={`p-inputswitch-slider ${
            this.props.checked
              ? this.props.sliderCheckedClassName
                ? styles.inputswitch_dark_theme_checked
                : null
              : this.props.sliderCheckedClassName
              ? styles.inputswitch_dark_theme_unchecked
              : null
          }`}></span>
      </div>
    );
  }
}
