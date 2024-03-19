import React, { useRef } from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import classNames from 'classnames';

const MultiSelectPanel = ({ appendTo, header, label, onClick, scrollHeight, children, panelClassName }) => {
  const elementRef = useRef(null); 

  const renderElement = () => (
    <div
      className={classNames('p-multiselect-panel p-hidden p-input-overlay', panelClassName)}
      onClick={onClick}
      ref={elementRef} 
    >
      {header}
      <div className="p-multiselect-items-wrapper" style={{ maxHeight: scrollHeight }}>
        <ul
          aria-label={label}
          aria-multiselectable={true}
          className="p-multiselect-items p-multiselect-list p-component"
          role="listbox">
          {children}
        </ul>
      </div>
    </div>
  );

  let element = renderElement();

  return appendTo ? ReactDOM.createPortal(element, appendTo) : element;
};

MultiSelectPanel.defaultProps = {
  appendTo: null,
  header: null,
  label: null,
  onClick: null,
  scrollHeight: null
};

MultiSelectPanel.propTypes = {
  appendTo: PropTypes.object,
  header: PropTypes.any,
  label: PropTypes.string,
  onClick: PropTypes.func,
  scrollHeight: PropTypes.string,
  panelClassName: PropTypes.string,
  children: PropTypes.node
};

export default MultiSelectPanel;
