import React, { forwardRef, memo } from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
import './DropdownPanel.scss';

const DropdownPanel = memo(
  forwardRef(
    ({ appendTo, children, filter, itemsWrapperRef, onClick, panelClassName, panelStyle, scrollHeight }, ref) => {
      const className = classNames('p-dropdown-panel p-hidden p-input-overlay', panelClassName);

      const element = (
        <div className={className} onClick={onClick} style={panelStyle} ref={ref}>
          {filter}
          <div className="p-dropdown-items-wrapper" style={{ maxHeight: scrollHeight || 'auto' }} ref={itemsWrapperRef}>
            <ul className="p-dropdown-items p-dropdown-list p-component">{children}</ul>
          </div>
        </div>
      );

      if (appendTo) {
        return ReactDOM.createPortal(element, appendTo);
      } else {
        return element;
      }
    }
  )
);

DropdownPanel.propTypes = {
  appendTo: PropTypes.object,
  filter: PropTypes.any,
  onClick: PropTypes.func,
  panelClassName: PropTypes.string,
  panelStyle: PropTypes.object,
  scrollHeight: PropTypes.string,
  children: PropTypes.node
};

DropdownPanel.defaultProps = {
  appendTo: null,
  filter: null,
  onClick: null,
  panelClassName: null,
  panelStyle: null,
  scrollHeight: null
};

export default DropdownPanel;