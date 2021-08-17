import { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Tooltip from 'primereact/tooltip';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';

export class InputTextarea extends Component {
  static defaultProps = {
    autoFocus: false,
    autoResize: false,
    collapsedHeight: 30,
    cols: 10,
    displayedHeight: 100,
    expandableOnClick: false,
    maxLength: 10000,
    moveCaretToEnd: false,
    onInput: null,
    rows: 1,
    tooltip: null,
    tooltipOptions: null,
    value: undefined
  };

  static propTypes = {
    autoResize: PropTypes.bool,
    expandableOnClick: PropTypes.bool,
    maxLength: PropTypes.number,
    moveCaretToEnd: PropTypes.bool,
    onInput: PropTypes.func,
    cols: PropTypes.number,
    rows: PropTypes.number,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    value: PropTypes.string
  };

  constructor(props) {
    super(props);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyUp = this.onKeyUp.bind(this);
    this.onInput = this.onInput.bind(this);
  }

  onFocus(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onFocus) {
      this.props.onFocus(e);
    }

    if (this.props.expandableOnClick) {
      this.element.style.height = `${this.props.displayedHeight}px`;
      this.element.style.boxShadow = 'var(--inputtextarea-box-shadow)';
    }
    if (this.props.moveCaretToEnd) {
      this.element.selectionStart = this.props.value.length;
      this.element.selectionEnd = this.props.value.length;
      this.element.scrollTop = this.element.scrollHeight;
    }
  }

  onBlur(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onBlur) {
      this.props.onBlur(e);
    }

    if (this.props.expandableOnClick) {
      this.element.style.height = `${this.props.collapsedHeight}px`;
      this.element.style.position = 'relative';
      this.element.style.boxShadow = '0px 0px';
    }
  }

  onKeyUp(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onKeyUp) {
      this.props.onKeyUp(e);
    }
  }

  onInput(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (!this.props.onChange) {
      if (e.target.value.length > 0) DomHandler.addClass(e.target, 'p-filled');
      else DomHandler.removeClass(e.target, 'p-filled');
    }

    if (this.props.onInput) {
      this.props.onInput(e);
    }
  }

  resize() {
    if (!this.cachedScrollHeight) {
      this.cachedScrollHeight = this.element.scrollHeight;
      this.element.style.overflow = 'hidden';
    }

    if (this.cachedScrollHeight !== this.element.scrollHeight) {
      this.element.style.height = '';
      this.element.style.height = this.element.scrollHeight + 'px';

      if (parseFloat(this.element.style.height) >= parseFloat(this.element.style.maxHeight)) {
        this.element.style.overflowY = 'scroll';
        this.element.style.height = this.element.style.maxHeight;
      } else {
        this.element.style.overflow = 'hidden';
      }

      this.cachedScrollHeight = this.element.scrollHeight;
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }

    if (this.props.autoResize) {
      this.resize();
    }
    this.element.style.height = `${this.props.collapsedHeight}px`;
  }

  componentDidUpdate(prevProps) {
    if (!DomHandler.isVisible(this.element)) {
      return;
    }

    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);
      else this.renderTooltip();
    }

    if (this.props.autoResize) {
      this.resize();
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
    const className = classNames('p-inputtext p-inputtextarea p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-filled':
        (this.props.value != null && this.props.value.toString().length > 0) ||
        (this.props.defaultValue != null && this.props.defaultValue.toString().length > 0),
      'p-inputtextarea-resizable': this.props.autoResize,
      'p-disabled p-filled':
        (this.props.disabled && this.props.value != null && this.props.value.toString().length > 0) ||
        (this.props.defaultValue != null && this.props.defaultValue.toString().length > 0)
    });

    let textareaProps = ObjectUtils.findDiffKeys(this.props, InputTextarea.defaultProps);

    return (
      <Fragment>
        <textarea
          {...textareaProps}
          autoFocus={this.props.autoFocus}
          className={className}
          id={this.props.id}
          maxLength={this.props.maxLength}
          onBlur={this.onBlur}
          onFocus={this.onFocus}
          onInput={this.onInput}
          onKeyUp={this.onKeyUp}
          ref={input => (this.element = input)}
          rows={this.props.rows}
          value={this.props.value}></textarea>
        <label className="srOnly" htmlFor={this.props.id}>
          {this.props.placeholder || this.props.id}
        </label>
      </Fragment>
    );
  }
}
