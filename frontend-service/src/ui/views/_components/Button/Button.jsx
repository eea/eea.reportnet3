import React from 'react';
import { isNull } from 'lodash';

import { config } from 'conf';

import { Button as PrimeButton } from 'primereact/button';
import { Icon } from 'ui/views/_components/Icon';

export const Button = ({
  disabled = false,
  icon = null,
  iconClasses = null,
  iconPos = 'left',
  label = null,
  onClick = () => {},
  className = null,
  type = 'button',
  tooltip = null,
  style = null,
  layout = null
}) => {
  const iconClassName = `${icon ? config.icons[icon] : ''} ${iconClasses ? iconClasses : ''}`;

  if (layout === 'simple') {
    return (
      <button className={className} disabled={disabled} label={label} onClick={onClick} style={style} type={type}>
        {icon ? <Icon icon={icon} /> : ''}
        {label}
      </button>
    );
  }
  if (isNull(layout)) {
    return (
      <PrimeButton
        className={className}
        disabled={disabled}
        icon={iconClassName}
        iconPos={icon ? iconPos : null}
        label={label}
        onClick={onClick}
        style={style}
        tooltip={tooltip}
        type={type}
      />
    );
  }
};
