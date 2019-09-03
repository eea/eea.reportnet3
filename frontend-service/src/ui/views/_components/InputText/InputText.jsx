import React from 'react';
import { InputText as PrimeInputText } from 'primereact/inputtext';

export const InputText = ({ type, value, onChange, onKeyPress, onInput, onBlur, onFocus }) => {
  return (
    <PrimeInputText
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onKeyPress={onKeyPress}
      onInput={onInput}
      type={type}
      value={value}
    />
  );
};
