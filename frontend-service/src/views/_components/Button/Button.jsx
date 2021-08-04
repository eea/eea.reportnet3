import isNull from 'lodash/isNull';

import './Button.scss';

import { config } from 'conf';

import { Button as PrimeButton } from 'primereact/button';
import { Icon } from 'views/_components/Icon';

export const Button = ({
  className = null,
  disabled = false,
  helpClassName = '',
  icon = null,
  iconClasses = null,
  iconPos = 'left',
  id = null,
  label = null,
  layout = null,
  onClick = () => {},
  onMouseDown = () => {},
  style = null,
  tabIndex = null,
  title = undefined,
  tooltip = null,
  tooltipOptions = null,
  type = 'button',
  value = '',
  visible = true
}) => {
  const iconClassName = `${icon ? config.icons[icon] : ''} ${iconClasses ? iconClasses : ''}`;
  if (layout === 'simple') {
    return (
      <button
        className={`${className} ${helpClassName}`}
        disabled={disabled}
        id={id}
        label={label}
        onClick={onClick}
        onMouseDown={onMouseDown}
        style={style}
        tabIndex={tabIndex}
        type={type}
        value={value}>
        {icon ? <Icon icon={icon} /> : null}
        <span className="srOnly">{value}</span>
        {label}
      </button>
    );
  }
  if (isNull(layout)) {
    return visible ? (
      <PrimeButton
        className={`${className} ${helpClassName}`}
        disabled={disabled}
        icon={iconClassName}
        iconPos={icon ? iconPos : null}
        id={id}
        label={label}
        onClick={onClick}
        onMouseDown={onMouseDown}
        style={style}
        tabIndex={tabIndex}
        title={title}
        tooltip={tooltip}
        tooltipOptions={tooltipOptions}
        type={type}
        value={value}
      />
    ) : null;
  }
};
