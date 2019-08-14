import React, { useContext } from 'react';

import { config } from 'assets/conf';

import { Button as PrimeButton } from 'primereact/button';

export const Button = ({ disabled, icon, iconClasses, label, onClick, className, type, tooltip, style }) => {
  const resources = useContext(ResourcesContext);
  const iconClassName = `${resources.icons[icon]} ${iconClasses ? iconClasses : ''}`;

  return (
    <PrimeButton
      className={className}
      disabled={disabled}
      icon={iconClassName}
      iconPos={iconPos}
      label={label}
      onClick={onClick}
      style={style}
      tooltip={tooltip}
      type={type}
    />
  );
};
