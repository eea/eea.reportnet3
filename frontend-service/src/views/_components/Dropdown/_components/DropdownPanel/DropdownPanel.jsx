import { Component } from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
import './DropdownPanel.scss';

export class DropdownPanel extends Component {
  static defaultProps = {
    appendTo: null,
    filter: null,
    onClick: null,
    panelClassName: null,
    panelStyle: null,
    scrollHeight: null
  };

  static propTypes = {
    appendTo: PropTypes.object,
    filter: PropTypes.any,
    onClick: PropTypes.func,
    panelClassName: PropTypes.string,
    // eslint-disable-next-line react/no-unused-prop-types
    panelstyle: PropTypes.object,
    scrollHeight: PropTypes.string
  };

  renderElement() {
    let className = classNames('p-dropdown-panel p-hidden p-input-overlay', this.props.panelClassName);

    return (
      <div
        className={className}
        onClick={this.props.onClick}
        ref={el => (this.element = el)}
        style={this.props.panelStyle}>
        {this.props.filter}
        <div
          className="p-dropdown-items-wrapper"
          ref={el => (this.itemsWrapper = el)}
          style={{ maxHeight: this.props.scrollHeight || 'auto' }}>
          <ul className="p-dropdown-items p-dropdown-list p-component">{this.props.children}</ul>
        </div>
      </div>
    );
  }

  render() {
    let element = this.renderElement();

    if (this.props.appendTo) {
      return ReactDOM.createPortal(element, this.props.appendTo);
    } else {
      return element;
    }
  }
}
