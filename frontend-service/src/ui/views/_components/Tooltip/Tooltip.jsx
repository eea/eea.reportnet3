import React, { useState } from 'react';
import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';

import './Tooltip.scss';

export const Tooltip = ({ label, children }) => {
  const [isVisible, setIsVisible] = useState(false);

  const isVisibleClass = isVisible ? ' is-visible' : ' is-hidden';
  const className = `tooltip ${isVisibleClass}`;

  const onMouseEnter = () => {
    setIsVisible(true);
  };

  const onMouseLeave = () => {
    setIsVisible(false);
  };

  return (
    <div className={className} onMouseEnter={onMouseEnter} onMouseLeave={onMouseLeave}>
      <span className="tooltip-label">{label}</span>
      {children}
    </div>
  );
};
