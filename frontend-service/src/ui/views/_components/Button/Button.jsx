import React from 'react';

import { config } from 'assets/conf';

import { Button as PrimeButton } from 'primereact/button';

export const Button = ({ disabled, icon, iconClasses, label, onClick, className, type, tooltip, style }) => {
  const iconClassName = `${config.icons[icon]} ${iconClasses ? iconClasses : ''}`;

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
