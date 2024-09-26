import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'primereact/tooltip';
import Editor from 'react-simple-code-editor';
import { highlight, languages } from 'prismjs';
import 'prismjs/components/prism-sql';
import 'prismjs/themes/prism.css';

export class SqlInputTextArea extends Component {
  static defaultProps = {
    autoFocus: false,
    autoResize: false,
    collapsedHeight: 30,
    displayedHeight: 100,
    moveCaretToEnd: false,
    onInput: null,
    tooltip: null,
    tooltipOptions: null,
    value: ''
  };

  static propTypes = {
    autoResize: PropTypes.bool,
    moveCaretToEnd: PropTypes.bool,
    onInput: PropTypes.func,
    tooltip: PropTypes.string,
    tooltipOptions: PropTypes.object,
    value: PropTypes.string,
    onChange: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);
    this.editorRef = React.createRef();
  }

  onFocus = e => {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onFocus) {
      this.props.onFocus(e);
    }

    if (this.props.moveCaretToEnd) {
      const input = this.editorRef.current._input;
      input.selectionStart = this.props.value.length;
      input.selectionEnd = this.props.value.length;
      input.scrollTop = input.scrollHeight;
    }
  };

  onBlur = e => {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onBlur) {
      this.props.onBlur(e);
    }
  };

  onKeyUp = e => {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onKeyUp) {
      this.props.onKeyUp(e);
    }
  };

  onInput = e => {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onChange) {
      this.props.onChange({ target: { value: e } });
    }
  };

  resize() {
    const input = this.editorRef.current._input;
    const scrollHeight = input.scrollHeight;

    if (!this.cachedScrollHeight) {
      this.cachedScrollHeight = scrollHeight;
      input.style.overflow = 'hidden';
    }

    if (this.cachedScrollHeight !== scrollHeight) {
      if (parseFloat(scrollHeight) >= parseFloat(input.style.maxHeight)) {
        input.style.overflowY = 'scroll';
      } else {
        input.style.overflow = 'hidden';
      }

      this.cachedScrollHeight = scrollHeight;
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }

    if (this.props.autoResize) {
      this.resize();
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.tooltip !== prevProps.tooltip) {
      if (this.tooltip) {
        this.tooltip.updateContent(this.props.tooltip);
      } else {
        this.renderTooltip();
      }
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
      target: this.editorRef.current._input,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    return (
      <Fragment>
        <div
          style={{
            overflow: 'auto',
            maxHeight: '440px'
          }}>
          <Editor
            highlight={e => highlight(e, languages.sql, 'sql')}
            onBlur={this.onBlur}
            onFocus={this.onFocus}
            onKeyUp={this.onKeyUp}
            onValueChange={this.onInput}
            padding={10}
            ref={this.editorRef}
            style={{
              fontFamily: '"Fira code", "Fira Mono", monospace',
              fontSize: 14,
              border: '1px solid #ccc',
              borderRadius: '4px',
              whiteSpace: 'pre-wrap',
              wordWrap: 'break-word',
              overflow: 'auto',
              minHeight: '440px'
            }}
            textareaId={this.props.id}
            value={this.props.value}
          />
        </div>
        <label className="srOnly" htmlFor={this.props.id}>
          {this.props.placeholder || this.props.id}
        </label>
      </Fragment>
    );
  }
}
