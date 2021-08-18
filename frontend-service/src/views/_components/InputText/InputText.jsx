import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import isNil from 'lodash/isNil';

import './InputText.scss';

import KeyFilter from 'views/_functions/PrimeReact/KeyFilter';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import { CharacterCounter } from 'views/_components/CharacterCounter';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Tooltip from 'primereact/tooltip';

import { relative } from 'path';

export class InputText extends Component {
  static defaultProps = {
    autoFocus: false,
    expandable: false,
    hasMaxCharCounter: false,
    id: null,
    keyfilter: null,
    maxLength: 10000,
    name: '',
    onInput: null,
    onKeyPress: null,
    required: false,
    tooltip: null,
    tooltipOptions: null,
    validateOnly: false,
    value: undefined
  };

  static propTypes = {
    autoFocus: PropTypes.bool,
    expandable: PropTypes.bool,
    hasMaxCharCounter: PropTypes.bool,
    id: PropTypes.string,
    keyfilter: PropTypes.any,
    maxLength: PropTypes.number,
    name: PropTypes.string,
    onInput: PropTypes.func,
    onKeyPress: PropTypes.func,
    required: PropTypes.bool,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    validateOnly: PropTypes.bool,
    value: PropTypes.any
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
    if (this.props.hasMaxCharCounter && !isNil(this.props.maxLength) && !isNil(this.inputElement)) {
      this.element.style.paddingRight = `${Number(this.inputElement.getBoundingClientRect().width) + 25}px`;
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
      <Fragment>
        <input
          autoComplete="off"
          autoFocus={this.props.autoFocus}
          ref={el => (this.element = el)}
          {...inputProps}
          className={className}
          id={this.props.id}
          onInput={this.onInput}
          onKeyPress={this.onKeyPress}
          value={this.props.value}
        />
        {this.props.required ? (
          <div style={{ position: relative, width: 0, height: 0 }}>
            <FontAwesomeIcon
              aria-label="required"
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
        {this.props.hasMaxCharCounter && !isNil(this.props.maxLength) ? (
          <CharacterCounter
            currentLength={this.props.value.length}
            inputRef={el => (this.inputElement = el)}
            maxLength={this.props.maxLength}
            style={{ top: '-30px' }}
          />
        ) : null}
        <label className="srOnly" htmlFor={this.props.id}>
          {this.props.name !== '' ? this.props.name : this.props.placeholder || this.props.id}
        </label>
      </Fragment>
    );
  }
}
