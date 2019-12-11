import React, { forwardRef } from 'react';
import { InputTextarea as PrimeInputTextarea } from 'primereact/inputtextarea';

const InputTextarea = forwardRef((props, _) => {
  const {
    autoFocus,
    autoResize,
    className,
    cols,
    disabled = false,
    inputTextareaRef,
    onBlur,
    onChange,
    onFocus,
    onInput,
    onKeyDown,
    placeholder,
    rows,
    type,
    value
  } = props;
  return (
    <PrimeInputTextarea
      autoResize={autoResize}
      autoFocus={autoFocus}
      className={className}
      cols={cols}
      disabled={disabled}
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onInput={onInput}
      onKeyDown={onKeyDown}
      placeholder={placeholder}
      ref={inputTextareaRef}
      rows={rows}
      type={type}
      value={value}
    />
  );
});

export { InputTextarea };
