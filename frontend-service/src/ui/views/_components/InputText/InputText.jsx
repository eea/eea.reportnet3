import React from 'react';
import { InputText as PrimeInputText } from 'primereact/inputtext';

export const InputText = ({
  autoFocus,
  className,
  disabled = false,
  onBlur,
  onChange,
  onFocus,
  onInput,
  onKeyDown,
  placeholder,
  type,
  value
}) => {
  return (
    <PrimeInputText
      autoFocus={autoFocus}
      className={className}
      disabled={disabled}
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onInput={onInput}
      onKeyDown={onKeyDown}
      placeholder={placeholder}
      type={type}
      value={value}
    />
  );
};
