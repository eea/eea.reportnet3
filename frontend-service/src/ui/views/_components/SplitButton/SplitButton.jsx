import React from 'react';
import { SplitButton as PrimeSplitButton } from 'primereact/splitbutton';

export const SplitButton = ({
  appendTo,
  className,
  disabled,
  icon,
  id,
  label,
  menuClassName,
  menuStyle,
  model,
  onClick,
  style,
  tabIndex,
  tooltip,
  tooltipOptions
}) => {
  return (
    <PrimeSplitButton
      appendTo={appendTo}
      className={className}
      disabled={disabled}
      icon={icon}
      id={id}
      label={label}
      menuClassName={menuClassName}
      menuStyle={menuStyle}
      model={model}
      onClick={onClick}
      style={style}
      tabIndex={tabIndex}
      tooltip={tooltip}
      tooltipOptions={tooltipOptions}
    />
  );
};
