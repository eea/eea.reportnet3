import React from 'react';
import { InputText as PrimeInputText } from 'primereact/inputtext';

export const InputText = ({ type, value, onBlur, onChange, onFocus, onKeyDown, onInput }) => {
  return (
    <PrimeInputText
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onKeyDown={onKeyDown}
      onInput={onInput}
      type={type}
      value={value}
    />
  );
};
