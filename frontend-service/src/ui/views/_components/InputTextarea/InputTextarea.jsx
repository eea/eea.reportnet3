// import React, { forwardRef } from 'react';
// import { InputTextarea as PrimeInputTextarea } from 'primereact/inputtextarea';

// const InputTextarea = forwardRef((props, _) => {
//   const {
//     autoFocus,
//     autoResize,
//     className,
//     cols,
//     disabled = false,
//     inputTextareaRef,
//     onBlur,
//     onChange,
//     onFocus,
//     onInput,
//     onKeyDown,
//     placeholder,
//     rows,
//     type,
//     value
//   } = props;
//   return (
//     <PrimeInputTextarea
//       autoResize={autoResize}
//       autoFocus={autoFocus}
//       className={className}
//       cols={cols}
//       disabled={disabled}
//       onBlur={onBlur}
//       onChange={onChange}
//       onFocus={onFocus}
//       onInput={onInput}
//       onKeyDown={onKeyDown}
//       placeholder={placeholder}
//       ref={inputTextareaRef}
//       rows={rows}
//       type={type}
//       value={value}
//     />
//   );
// });

// export { InputTextarea };

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Tooltip from 'primereact/tooltip';
import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';
import ObjectUtils from 'ui/views/_functions/PrimeReact/ObjectUtils';

export class InputTextarea extends Component {
  static defaultProps = {
    autoFocus: false,
    autoResize: false,
    collapsedHeight: 30,
    cols: 10,
    displayedHeight: 100,
    expandableOnClick: false,
    onInput: null,
    rows: 1,
    tooltip: null,
    tooltipOptions: null
  };

  static propTypes = {
    autoResize: PropTypes.bool,
    expandableOnClick: PropTypes.bool,
    onInput: PropTypes.func,
    cols: PropTypes.number,
    rows: PropTypes.number,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object
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
      this.element.style.boxShadow = '0 10px 6px -6px rgba(var(--blue-120-hex), 0.2)';
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
      'p-inputtextarea-resizable': this.props.autoResize
    });

    let textareaProps = ObjectUtils.findDiffKeys(this.props, InputTextarea.defaultProps);

    return (
      <textarea
        {...textareaProps}
        autoFocus={this.props.autoFocus}
        className={className}
        ref={input => (this.element = input)}
        onFocus={this.onFocus}
        onBlur={this.onBlur}
        onKeyUp={this.onKeyUp}
        onInput={this.onInput}></textarea>
    );
  }
}
