import { useState, useRef } from 'react';
import PropTypes from 'prop-types';

import classNames from 'classnames';

export const RadioButton = ({
  ariaLabelledBy,
  checked,
  className,
  disabled,
  id,
  inputId,
  name,
  onChange,
  required,
  style,
  tabIndex,
  value
}) => {
  const [focused, setFocused] = useState(false);

  const boxRef = useRef(null);
  const containerRef = useRef(null);
  const inputRef = useRef(null);

  let containerClass = classNames(
    'p-radiobutton p-component',
    { 'p-radiobutton-checked': checked, 'p-radiobutton-disabled': disabled, 'p-radiobutton-focused': focused },
    className
  );

  let boxClass = classNames('p-radiobutton-box', {
    'p-disabled': disabled,
    'p-focus': focused,
    'p-highlight': checked
  });

  const onRadioClick = event => {
    if (!disabled && onChange) {
      onChange({
        checked: !checked,
        originalEvent: event,
        preventDefault: () => {},
        stopPropagation: () => {},
        target: { name, id, value, checked: !checked },
        value
      });

      inputRef.current.checked = !checked;
      inputRef.current.focus();
    }
  };

  const onFocus = () => setFocused(true);

  const onBlur = () => setFocused(false);

  return (
    <div className={containerClass} id={id} onClick={() => onRadioClick()} ref={containerRef} style={style}>
      <div className="p-hidden-accessible">
        <input
          aria-labelledby={ariaLabelledBy}
          defaultChecked={checked}
          disabled={disabled}
          id={inputId}
          name={name}
          onBlur={onBlur}
          onFocus={onFocus}
          ref={inputRef}
          required={required}
          tabIndex={tabIndex}
          type="radio"
        />
      </div>
      <div aria-checked={checked} className={boxClass} ref={boxRef} role="radio">
        <div className="p-radiobutton-icon" />
      </div>
    </div>
  );
};

RadioButton.propTypes = {
  ariaLabelledBy: PropTypes.string,
  checked: PropTypes.bool,
  className: PropTypes.string,
  disabled: PropTypes.bool,
  id: PropTypes.string,
  inputId: PropTypes.string,
  onChange: PropTypes.func,
  required: PropTypes.bool,
  style: PropTypes.object,
  tabIndex: PropTypes.number,
  value: PropTypes.any
};

RadioButton.defaultProps = {
  ariaLabelledBy: null,
  checked: false,
  className: null,
  disabled: false,
  id: null,
  inputId: null,
  name: null,
  onChange: null,
  required: false,
  style: null,
  tabIndex: null,
  value: null
};
