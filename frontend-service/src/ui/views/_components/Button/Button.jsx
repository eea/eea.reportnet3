import React from 'react';

import { config } from 'conf';

import { Button as PrimeButton } from 'primereact/button';

export const Button = ({ disabled, icon, iconClasses, iconPos, label, onClick, className, type, tooltip, style }) => {
  const iconClassName = `${config.icons[icon]} ${iconClasses ? iconClasses : ''}`;

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
