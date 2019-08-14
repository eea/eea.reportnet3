import React, { useContext } from 'react';

import { Button as PrimeButton } from 'primereact/button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const Button = ({ disabled, icon, iconClasses, label, onClick, className, type, tooltip, style }) => {
  const resources = useContext(ResourcesContext);
  const iconClassName = `${resources.icons[icon]} ${iconClasses ? iconClasses : ''}`;

  return (
    <PrimeButton
      className={className}
      disabled={disabled}
      icon={iconClassName}
      label={label}
      onClick={onClick}
      style={style}
      tooltip={tooltip}
      type={type}
    />
  );
};
