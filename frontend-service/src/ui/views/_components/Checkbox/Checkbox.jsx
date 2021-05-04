import { Checkbox as PrimeCheckbox } from 'primereact/checkbox';

import './Checkbox.scss';

const Checkbox = ({
  className,
  defaultChecked,
  disabled,
  htmlFor,
  id,
  inputId,
  isChecked,
  labelClassName,
  labelMessage,
  onChange,
  style
}) => {
  return <PrimeCheckbox checked={isChecked} disabled={disabled} id={id} inputId={inputId} onChange={onChange} />;
};

export { Checkbox };
