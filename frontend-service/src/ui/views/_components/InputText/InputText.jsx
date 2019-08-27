import React from 'react';
import { InputText as PrimeInputText } from 'primereact/inputtext';

export const InputText = ({ type, value, onChange }) => {
  return <PrimeInputText type={type} value={value} onChange={onChange} />;
};
