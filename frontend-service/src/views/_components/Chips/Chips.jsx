import { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import isNil from 'lodash/isNil';

import styles from './Chips.module.scss';
import './Chips.scss';

import { InputText } from 'views/_components/InputText';
import Tooltip from 'primereact/tooltip';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';
import classNames from 'classnames';

const Chips = ({
  ariaLabelledBy = null,
  checkForDuplicates = false,
  className = null,
  clearOnPaste = false,
  disabled = null,
  deleteWhiteSpaces = false,
  errorMessage = 'Error',
  forbiddenChar = false,
  id = null,
  inputClassName = null,
  itemTemplate = null,
  max = null,
  name = null,
  onAdd = null,
  onBlur = null,
  onChange = null,
  onFocus = null,
  onRemove = null,
  pasteSeparator = ',',
  placeholder = null,
  showErrorMessage = false,
  style = null,
  tooltip = null,
  tooltipOptions = null,
  value = null
}) => {
  const inputElement = useRef();
  const listElement = useRef();
  const [hasErrors, setHasErrors] = useState(false);
  useEffect(() => {
    if (!isNil(tooltip)) {
      renderTooltip();
    }
  }, []);

  const onKeyDownChips = event => {
    const inputValue = deleteWhiteSpaces ? event.target.value.trim() : event.target.value;

    switch (event.which) {
      //backspace
      case 8:
        if (inputElement.current.element.value.length === 0 && value && value.length > 0) {
          removeItem(event, value.length - 1);
        } else {
          if (checkForDuplicates) setHasErrors(false);
        }
        break;
      //enter
      case 9:
      case 13:
        if (inputValue && inputValue.trim().length && (!max || max > value.length)) {
          const values = [...value];
          if (checkForDuplicates && values.indexOf(inputValue) > -1) {
            setHasErrors(true);
            return;
          } else {
            values.push(inputValue);

            if (!isNil(onAdd)) {
              onAdd({
                originalEvent: event,
                value: inputValue
              });
            }

            if (!isNil(onChange)) {
              onChange({
                originalEvent: event,
                value: values,
                stopPropagation: () => {},
                preventDefault: () => {},
                target: {
                  name: name,
                  id: id,
                  value: values
                }
              });
            }
            setHasErrors(false);
          }
        }

        inputElement.current.element.value = '';
        event.preventDefault();
        break;
      default:
        if (isMaxedOut()) {
          event.preventDefault();
        }
        break;
    }
  };

  const onFocusChips = event => {
    DomHandler.addClass(listElement.current, 'p-focus');
    if (!isNil(onFocus)) {
      onFocus(event);
    }
  };

  const onBlurChips = event => {
    DomHandler.removeClass(listElement.current, 'p-focus');

    const inputValue = deleteWhiteSpaces
      ? forbiddenChar
        ? event.target.value.trim().split(pasteSeparator).join('')
        : event.target.value.trim()
      : event.target.value;

    if (inputValue && inputValue.trim().length && (!max || max > value.length)) {
      const values = [...value];
      if (checkForDuplicates && values.indexOf(inputValue) > -1) {
        setHasErrors(true);
        return;
      } else {
        values.push(inputValue);

        if (!isNil(onAdd)) {
          onAdd({
            originalEvent: event,
            value: inputValue
          });
        }

        if (!isNil(onChange)) {
          onChange({
            originalEvent: event,
            value: values,
            stopPropagation: () => {},
            preventDefault: () => {},
            target: {
              name: name,
              id: id,
              value: values
            }
          });
        }
        setHasErrors(false);
      }
    }

    inputElement.current.element.value = '';
    event.preventDefault();

    if (!isNil(onBlur)) {
      onBlur(event);
    }
  };

  const isMaxedOut = () => {
    return max && value && max === value.length;
  };

  const removeItem = (event, index) => {
    if (disabled) {
      return;
    }

    const values = [...value];
    const removedItem = values.splice(index, 1);

    if (!isNil(onRemove)) {
      onRemove({
        originalEvent: event,
        value: removedItem
      });
    }

    if (!isNil(onChange)) {
      onChange({
        originalEvent: event,
        value: values,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: name,
          id: id,
          value: values
        }
      });
    }
  };

  const focusInput = () => {
    !isNil(inputElement.current) && inputElement.current.element.focus();
  };

  const renderErrorMessage = () => {
    return <span className={styles.errorMessage}>{errorMessage}</span>;
  };

  const renderInputElement = () => {
    return (
      <li className="p-chips-input-token">
        <InputText
          aria-hidden={disabled || isMaxedOut()}
          aria-labelledby={ariaLabelledBy}
          className={hasErrors ? styles.chipsTokenError : null}
          disabled={disabled || isMaxedOut()}
          id={name}
          keyfilter={forbiddenChar ? (pasteSeparator === ',' ? 'noComma' : 'noSemicolon') : ''}
          name={name}
          onBlur={onBlurChips}
          onFocus={onFocusChips}
          onKeyDown={onKeyDownChips}
          onPaste={e => {
            if (clearOnPaste) e.preventDefault();
          }}
          placeholder={placeholder}
          ref={inputElement}
          type="text"
        />
      </li>
    );
  };

  const renderItem = (value, index) => {
    const content = itemTemplate ? itemTemplate(value) : value;
    const icon = disabled ? null : (
      <span className="p-chips-token-icon pi pi-fw pi-times" onClick={event => removeItem(event, index)}></span>
    );

    return (
      <li
        className="p-chips-token p-highlight"
        key={index}
        onMouseDownCapture={e => {
          if (e.button === 1) {
            e.preventDefault();
            removeItem(e, index);
          }
        }}>
        {icon}
        <span className="p-chips-token-label">{content}</span>
      </li>
    );
  };

  const renderItems = () => {
    if (value) {
      return value.map((value, index) => {
        return renderItem(value, index);
      });
    } else {
      return null;
    }
  };

  const renderList = () => {
    const className = classNames(`p-inputtext ${inputClassName}`, { 'p-disabled': disabled });
    const items = renderItems();
    const inputElement = renderInputElement();
    if (value) {
      return (
        <ul className={className} onClick={focusInput} ref={listElement}>
          {items}
          {!disabled && inputElement}
          {hasErrors && showErrorMessage && renderErrorMessage()}
        </ul>
      );
    } else {
      return null;
    }
  };

  const renderTooltip = () => {
    new Tooltip({
      target: inputElement.current.element,
      targetContainer: listElement.current.element,
      content: tooltip,
      options: tooltipOptions
    });
  };

  return (
    <div className={`p-chips p-component ${className}`} id={id} style={style}>
      {renderList()}
    </div>
  );
};

Chips.propTypes = {
  ariaLabelledBy: PropTypes.string,
  className: PropTypes.string,
  clearOnPaste: PropTypes.bool,
  disabled: PropTypes.bool,
  errorMessage: PropTypes.string,
  id: PropTypes.string,
  itemTemplate: PropTypes.func,
  max: PropTypes.number,
  name: PropTypes.string,
  onAdd: PropTypes.func,
  onBlur: PropTypes.func,
  onChange: PropTypes.func,
  onFocus: PropTypes.func,
  onRemove: PropTypes.func,
  pasteSeparator: PropTypes.string,
  placeholder: PropTypes.string,
  showErrorMessage: PropTypes.bool,
  style: PropTypes.object,
  tooltip: PropTypes.string,
  tooltipOptions: PropTypes.object,
  value: PropTypes.array
};

export { Chips };
