// import React, { forwardRef } from 'react';
// import { InputText as PrimeInputText } from 'primereact/inputtext';

// export const InputText = forwardRef((props, _) => {
//   const {
//     autoFocus,
//     className,
//     disabled = false,
//     inputRef,
//     onBlur,
//     onChange,
//     onFocus,
//     onInput,
//     onKeyDown,
//     placeholder,
//     type,
//     value
//   } = props;
//   return (
//     <PrimeInputText
//       autoFocus={autoFocus}
//       className={className}
//       disabled={disabled}
//       onBlur={onBlur}
//       onChange={onChange}
//       onFocus={onFocus}
//       onInput={onInput}
//       onKeyDown={onKeyDown}
//       placeholder={placeholder}
//       type={type}
//       ref={inputRef}
//       value={value}
//     />
//   );
// });

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';

import styles from './InputText.module.css';

import logo from 'assets/images/logo.png';

import KeyFilter from 'ui/views/_functions/PrimeReact/KeyFilter';
import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'ui/views/_functions/PrimeReact/ObjectUtils';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import Tooltip from 'primereact/tooltip';

export class InputText extends Component {
  static defaultProps = {
    autoFocus: false,
    expandable: false,
    onInput: null,
    onKeyPress: null,
    keyfilter: null,
    required: false,
    validateOnly: false,
    tooltip: null,
    tooltipOptions: null
  };

  static propTypes = {
    autoFocus: PropTypes.bool,
    expandable: PropTypes.bool,
    onInput: PropTypes.func,
    onKeyPress: PropTypes.func,
    keyfilter: PropTypes.any,
    required: PropTypes.bool,
    validateOnly: PropTypes.bool,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object
  };

  constructor(props) {
    super(props);
    this.onBlur = this.onBlur.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onInput = this.onInput.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
  }

  onKeyPress(event) {
    if (this.props.onKeyPress) {
      this.props.onKeyPress(event);
    }

    if (this.props.keyfilter) {
      KeyFilter.onKeyPress(event, this.props.keyfilter, this.props.validateOnly);
    }
  }

  onBlur(event) {
    console.log(this.element);
    if (this.props.expandable) {
      this.element.style.height = '24px';
      this.element.style.position = 'relative';
    }
  }

  onFocus(event) {
    if (this.props.expandable) {
      this.element.style.height = '100px';
      this.element.style.position = 'absolute';
      this.element.style.zIndex = '9999';
      this.element.style.maxWidth = this.element.clientWidth;
      this.element.style.wordBreak = 'break-word';
    }
  }

  onInput(event) {
    let validatePattern = true;
    if (this.props.keyfilter && this.props.validateOnly) {
      validatePattern = KeyFilter.validate(event, this.props.keyfilter);
    }

    if (this.props.onInput) {
      this.props.onInput(event, validatePattern);
    }

    if (!this.props.onChange) {
      if (event.target.value.length > 0) DomHandler.addClass(event.target, 'p-filled');
      else DomHandler.removeClass(event.target, 'p-filled');
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
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    const className = classNames('p-inputtext p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-filled':
        (this.props.value != null && this.props.value.toString().length > 0) ||
        (this.props.defaultValue != null && this.props.defaultValue.toString().length > 0)
    });

    let inputProps = ObjectUtils.findDiffKeys(this.props, InputText.defaultProps);
    return (
      <React.Fragment>
        <input
          autoFocus={this.props.autoFocus}
          ref={el => (this.element = el)}
          {...inputProps}
          className={`${className}${this.props.required ? ` ${styles.required}` : ''}`}
          onBlur={this.onBlur}
          onFocus={this.onFocus}
          onInput={this.onInput}
          onKeyPress={this.onKeyPress}
          // style={{
          //   // background: this.props.required ? `url(${logo}) no-repeat scroll 7px 7px !important;` : ''
          //   backgroundImage: `url(${logo})`
          // }}
        />
        {this.props.required ? (
          <FontAwesomeIcon
            icon={AwesomeIcons('infoCircle')}
            style={{
              float: 'right',
              marginTop: '2px',
              color: 'var(--errors)',
              position: 'absolute',
              left: this.element ? this.element.clientWidth : '',
              top: this.element ? this.element.offsetHeight : ''
            }}
          />
        ) : null}
      </React.Fragment>
    );
  }
}
