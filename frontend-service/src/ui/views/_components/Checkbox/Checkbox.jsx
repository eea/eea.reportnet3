import React from 'react';
import { Checkbox as PrimeCheckbox } from 'primereact/checkbox';

const Checkbox = ({ id, className, style, defaultChecked, onChange, htmlFor, isChecked, disabled }) => {
  return (
    <>
      <PrimeCheckbox onChange={onChange} checked={isChecked} disabled={disabled} />
    </>
  );
};

export { Checkbox };
