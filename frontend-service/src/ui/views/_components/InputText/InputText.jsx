import React, { forwardRef } from 'react';
import { InputText as PrimeInputText } from 'primereact/inputtext';

export const InputText = forwardRef((props, _) => {
  const {
    autoFocus,
    className,
    disabled = false,
    inputRef,
    onBlur,
    onChange,
    onFocus,
    onInput,
    onKeyDown,
    placeholder,
    type,
    value
  } = props;
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
      ref={inputRef}
      value={value}
    />
  );
});
