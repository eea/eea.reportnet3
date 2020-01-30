import React from 'react';

const Checkbox = ({
  id,
  className,
  style,
  defaultChecked,
  onChange,
  htmlFor,
  labelMessage,
  isChecked,
  labelClassName
}) => {
  return (
    <React.Fragment>
      <input
        className={className}
        defaultChecked={defaultChecked}
        id={id}
        checked={isChecked}
        onChange={onChange}
        style={style}
        type="checkbox"
      />
      <label htmlFor={htmlFor} className={labelClassName}>
        {labelMessage}
      </label>
    </React.Fragment>
  );
};

export { Checkbox };
