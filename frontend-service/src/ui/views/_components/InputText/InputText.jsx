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

import './InputText.css';

import KeyFilter from 'ui/views/_functions/PrimeReact/KeyFilter';
import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'ui/views/_functions/PrimeReact/ObjectUtils';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Tooltip from 'primereact/tooltip';

import { relative } from 'path';

export class InputText extends Component {
  static defaultProps = {
    autoFocus: false,
    expandable: false,
    id: '',
    onInput: null,
    onKeyPress: null,
    keyfilter: null,
    maxLength: 10000,
    required: false,
    validateOnly: false,
    tooltip: null,
    tooltipOptions: null
  };

  static propTypes = {
    autoFocus: PropTypes.bool,
    expandable: PropTypes.bool,
    id: PropTypes.string,
    maxLength: PropTypes.number,
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
          autoComplete="off"
          autoFocus={this.props.autoFocus}
          ref={el => (this.element = el)}
          {...inputProps}
          className={className}
          id={this.props.id}
          onInput={this.onInput}
          onKeyPress={this.onKeyPress}
        />
        {this.props.required ? (
          <div style={{ position: relative, width: 0, height: 0 }}>
            <FontAwesomeIcon
              icon={AwesomeIcons('infoCircle')}
              style={{
                color: 'var(--errors)',
                position: 'relative',
                right: '40px',
                opacity: `${this.props.disabled ? 0.3 : 1}`
              }}
            />
          </div>
        ) : null}
      </React.Fragment>
    );
  }
}
