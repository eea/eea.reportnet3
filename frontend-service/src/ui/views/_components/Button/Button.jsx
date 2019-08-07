import React, { useContext } from 'react';

import { Button as PrimeButton } from 'primereact/button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const Button = ({ disabled, icon, iconClasses, label, onClick }) => {
  const resources = useContext(ResourcesContext);
  let icons = [
    resources.icons['export'],
    resources.icons['import'],
    resources.icons['trash'],
    resources.icons['warning'],
    resources.icons['clock'],
    resources.icons['dashboard'],
    resources.icons['eye'],
    resources.icons['filter'],
    resources.icons['group-by'],
    resources.icons['sort'],
    resources.icons['validate'],
    resources.icons['refresh'],
    resources.icons['camera']
  ];

  let disabledButton = disabled ? true : false;
  let classes = `p-button-rounded p-button-secondary`;
  const iconClassName = `${icons[icon]} ${iconClasses ? iconClasses : ''}`;

  return (
    <PrimeButton
      className={classes}
      icon={iconClassName}
      label={label}
      style={{ marginRight: '.25em' }}
      onClick={onClick}
      disabled={disabledButton}
    />
  );
};
