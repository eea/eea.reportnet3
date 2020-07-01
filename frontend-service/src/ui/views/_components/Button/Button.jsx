import React from 'react';
import { isNull } from 'lodash';

import './Button.scss';

import { config } from 'conf';

import { Button as PrimeButton } from 'primereact/button';
import { Icon } from 'ui/views/_components/Icon';

export const Button = ({
  id = null,
  disabled = false,
  icon = null,
  iconClasses = null,
  iconPos = 'left',
  label = null,
  onClick = () => {},
  onMouseDown = () => {},
  className = null,
  type = 'button',
  tooltip = null,
  tooltipOptions = null,
  style = null,
  layout = null,
  value = '',
  visible = true
}) => {
  const iconClassName = `${icon ? config.icons[icon] : ''} ${iconClasses ? iconClasses : ''}`;
  if (layout === 'simple') {
    return (
      <button
        className={className}
        disabled={disabled}
        id={id}
        label={label}
        onClick={onClick}
        onMouseDown={onMouseDown}
        style={style}
        type={type}
        value={value}>
        {icon ? <Icon icon={icon} /> : ''}
        <span className="srOnly">{value}</span>
        {label}
      </button>
    );
  }
  if (isNull(layout)) {
    return visible ? (
      <>
        <PrimeButton
          id={id}
          className={className}
          disabled={disabled}
          icon={iconClassName}
          iconPos={icon ? iconPos : null}
          label={label}
          onClick={onClick}
          onMouseDown={onMouseDown}
          style={style}
          tooltip={tooltip}
          type={type}
          tooltipOptions={tooltipOptions}
          value={value}
        />
        <span className="srOnly">Email</span>
      </>
    ) : null;
  }
};
