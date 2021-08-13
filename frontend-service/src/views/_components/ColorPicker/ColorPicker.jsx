import { ColorPicker as PrimeColorPicker } from 'primereact/colorpicker';

export const ColorPicker = ({
  className,
  defaultColor,
  disabled,
  id,
  key,
  onChange,
  styles,
  tooltip,
  tooltipOptions,
  value
}) => {
  return (
    <PrimeColorPicker
      className={className}
      defaultColor={defaultColor}
      disabled={disabled}
      id={id}
      key={key}
      onChange={onChange}
      styles={styles}
      tooltip={tooltip}
      tooltipOptions={tooltipOptions}
      value={value}
    />
  );
};
