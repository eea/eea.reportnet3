import React from 'react';
import { InputSwitch as PrimeInputSwitch } from 'primereact/inputswitch';

const InputSwitch = ({ checked, className, disabled, onChange, style }) => {
  return (
    <PrimeInputSwitch checked={checked} disabled={disabled} onChange={onChange} className={className} style={style} />
  );
};

export { InputSwitch };
