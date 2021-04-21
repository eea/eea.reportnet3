import { Component } from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
import './DropdownPanel.scss';

export class DropdownPanel extends Component {
  static defaultProps = {
    appendTo: null,
    filter: null,
    scrollHeight: null,
    panelClassName: null,
    panelStyle: null,
    onClick: null
  };

  static propTypes = {
    appendTo: PropTypes.object,
    filter: PropTypes.any,
    scrollHeight: PropTypes.string,
    panelClassName: PropTypes.string,
    panelstyle: PropTypes.object,
    onClick: PropTypes.func
  };

  renderElement() {
    let className = classNames('p-dropdown-panel p-hidden p-input-overlay', this.props.panelClassName);

    return (
      <div
        ref={el => (this.element = el)}
        className={className}
        style={this.props.panelStyle}
        onClick={this.props.onClick}>
        {this.props.filter}
        <div
          ref={el => (this.itemsWrapper = el)}
          className="p-dropdown-items-wrapper"
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

// import  { useRef } from 'react';
// import PropTypes from 'prop-types';
// import ReactDOM from 'react-dom';
// import classNames from 'classnames';

// export const DropdownPanel = ({
//   appendTo = null,
//   children,
//   filter = null,
//   onClick = null,
//   panelClassName = null,
//   panelStyle = null,
//   scrollHeight = null
// }) => {
//   const itemsWrapper = useRef();
//   const element = useRef();
//   const renderElement = () => {
//     let className = classNames('p-dropdown-panel p-hidden p-input-overlay', panelClassName);

//     return (
//       <div ref={element} className={className} style={panelStyle} onClick={onClick}>
//         {filter}
//         <div ref={itemsWrapper} className="p-dropdown-items-wrapper" style={{ maxHeight: scrollHeight || 'auto' }}>
//           <ul className="p-dropdown-items p-dropdown-list p-component">{children}</ul>
//         </div>
//       </div>
//     );
//   };
//   let element = renderElement();

//   if (appendTo) {
//     return ReactDOM.createPortal(elementDOM, appendTo);
//   } else {
//     return elementDOM;
//   }
// };

// DropdownPanel.propTypes = {
//   appendTo: PropTypes.object,
//   filter: PropTypes.any,
//   scrollHeight: PropTypes.string,
//   panelClassName: PropTypes.string,
//   panelstyle: PropTypes.object,
//   onClick: PropTypes.func
// };
