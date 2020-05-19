import React, { useEffect, useRef, useState } from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import isNil from 'lodash/isNil';

import styles from './Chips.module.scss';
import './Chips.scss';

import { InputText } from 'ui/views/_components/InputText';
import Tooltip from 'primereact/tooltip';

import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';
import classNames from 'classnames';

const Chips = ({
  ariaLabelledBy = null,
  checkForDuplicates = false,
  className = null,
  disabled = null,
  deleteWhiteSpaces = false,
  forbiddenCommas = false,
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
  placeholder = null,
  style = null,
  tooltip = null,
  tooltipOptions = null,
  value = null
}) => {
  let myTooltip;
  const inputElement = useRef();
  const listElement = useRef();
  //   const [values, setValues] = useState();
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
        }
        break;

      //enter
      case 9:
      case 13:
        if (inputValue && inputValue.trim().length && (!max || max > value.length)) {
          let values = [...value];
          if (checkForDuplicates && values.indexOf(inputValue) > -1) {
            setHasErrors(true);
            return;
          } else {
            values.push(inputValue);
            //   setValues({ values: values });

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
      ? forbiddenCommas
        ? event.target.value.trim().split(',').join('')
        : event.target.value.trim()
      : event.target.value;

    console.log({ inputValue });

    if (inputValue && inputValue.trim().length && (!max || max > value.length)) {
      let values = [...value];
      if (checkForDuplicates && values.indexOf(inputValue) > -1) {
        setHasErrors(true);
        return;
      } else {
        values.push(inputValue);
        //   setValues({ values: values });

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

    let values = [...value];
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
    inputElement.current.element.focus();
  };

  const renderInputElement = () => {
    return (
      <li className="p-chips-input-token">
        <InputText
          aria-labelledby={ariaLabelledBy}
          className={hasErrors ? styles.chipsTokenError : ''}
          disabled={disabled || isMaxedOut()}
          keyfilter={forbiddenCommas ? 'noComma' : ''}
          name={name}
          onBlur={onBlurChips}
          onFocus={onFocusChips}
          onKeyDown={onKeyDownChips}
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
        key={index}
        className="p-chips-token p-highlight"
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
        <ul ref={listElement} className={className} onClick={focusInput}>
          {items}
          {inputElement}
        </ul>
      );
    } else {
      return null;
    }
  };

  const renderTooltip = () => {
    myTooltip = new Tooltip({
      target: inputElement.current.element,
      targetContainer: listElement.current.element,
      content: tooltip,
      options: tooltipOptions
    });
  };

  return (
    <div id={id} className={`p-chips p-component ${className}`} style={style}>
      {renderList()}
    </div>
  );
};

Chips.propTypes = {
  id: PropTypes.string,
  name: PropTypes.string,
  placeholder: PropTypes.string,
  value: PropTypes.array,
  max: PropTypes.number,
  disabled: PropTypes.bool,
  style: PropTypes.object,
  className: PropTypes.string,
  tooltip: PropTypes.string,
  tooltipOptions: PropTypes.object,
  ariaLabelledBy: PropTypes.string,
  itemTemplate: PropTypes.func,
  onAdd: PropTypes.func,
  onRemove: PropTypes.func,
  onChange: PropTypes.func,
  onFocus: PropTypes.func,
  onBlur: PropTypes.func
};

export { Chips };
