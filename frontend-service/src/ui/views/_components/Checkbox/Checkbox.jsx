import React from 'react';
import { Checkbox as PrimeCheckbox } from 'primereact/checkbox';

import './Checkbox.scss';

const Checkbox = ({
  className,
  defaultChecked,
  disabled,
  htmlFor,
  id,
  isChecked,
  labelClassName,
  labelMessage,
  onChange,
  style
}) => {
  return (
    <>
      <PrimeCheckbox onChange={onChange} checked={isChecked} disabled={disabled} />
    </>
  );
};

export { Checkbox };
