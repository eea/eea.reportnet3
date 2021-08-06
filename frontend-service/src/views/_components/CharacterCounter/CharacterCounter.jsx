import React, { useState, useCallback } from 'react';
import PropTypes from 'prop-types';

const NEW_LINE_REGEX = /(\r\n|\n|\r)/g;

export const CharavterCounter = props => {
  const {
    showCount,
    countLimit,
    countDirection,
    countPosition,
    shouldTruncate,
    initialValue,
    rows,
    resize,
    className,
    placeholder,
    onCount,
    onChange,
    onFocus,
    onBlur,
    required,
    disabled,
    ...remainingProps
  } = props;

  const [value, setValue] = useState(shouldTruncate ? initialValue.slice(0, countLimit) : initialValue);

  const hasOverflowed = countLimit - value.length <= 0;
  const isAscending = countDirection === 'asc';
  const counter = isAscending ? value.length : countLimit - value.length;

  const handleChange = useCallback(
    event => {
      let newValue = event.target.value.replace(NEW_LINE_REGEX, '\n');

      if (shouldTruncate) {
        newValue = newValue.slice(0, countLimit);
      }

      event.target.value = newValue;

      setValue(newValue);
      onChange(event);
    },
    [countLimit, onChange, shouldTruncate]
  );

  return (
    <div className={className}>
      {showCount && (
        <div
          data-testid="counter"
          style={{
            textAlign: countPosition,
            color: hasOverflowed ? 'red' : 'black'
          }}>
          {counter}/{countLimit}
        </div>
      )}
    </div>
  );
};
