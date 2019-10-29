import React from 'react';
import { InputSwitch as PrimeInputSwitch } from 'primereact/inputswitch';

const InputSwitch = ({ checked, className, onChange, style }) => {
  return <PrimeInputSwitch checked={checked} onChange={onChange} className={className} style={style} />;
};

export { InputSwitch };
