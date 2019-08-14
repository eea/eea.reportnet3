import React, { useContext } from 'react';

import { config } from 'assets/conf';

import { Button as PrimeButton } from 'primereact/button';

export const Button = ({ disabled, icon, iconPos, iconClasses, label, onClick }) => {
  let icons = [
    config.icons.export,
    config.icons.import,
    config.icons.trash,
    config.icons.warning,
    config.icons.clock,
    config.icons.dashboard,
    config.icons.eye,
    config.icons.filter,
    config.icons.groupBy,
    config.icons.sort,
    config.icons.validate,
    config.icons.refresh,
    config.icons.camera
  ];

  let disabledButton = disabled ? true : false;
  let classes = `p-button-rounded p-button-secondary`;
  const iconClassName = `${icons[icon]} ${iconClasses ? iconClasses : ''}`;

  return (
    <PrimeButton
      className={classes}
      icon={iconClassName}
      iconPos={iconPos}
      label={label}
      style={{ marginRight: '.25em' }}
      onClick={onClick}
      disabled={disabledButton}
    />
  );
};
