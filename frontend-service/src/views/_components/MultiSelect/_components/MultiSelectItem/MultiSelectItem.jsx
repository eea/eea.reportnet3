import classNames from 'classnames';

import styles from './MultiSelectItem.module.scss';
import { memo } from 'react';

export const MultiSelectItem = memo(({
  disabled = false,
  label,
  onClick,
  onKeyDown,
  option,
  selected,
  tabIndex,
  template
}) => {
  const getClassNameLabel = () => {
    if (selected) {
      return styles.itemSelected;
    }
  };

  const onClickEvent = event => {
    if (onClick && !disabled) {
      onClick({
        originalEvent: event,
        option: option
      });
    }
    event.preventDefault();
  };

  const onKeyDownEvent = event => {
    if (onKeyDown && !disabled) {
      onKeyDown({
        originalEvent: event,
        option: option
      });
    }
  };

  const renderCheckbox = () => {
    if (!disabled) {
      return (
        <div className="p-checkbox p-component">
          <div className={checkboxClassName}>
            <span aria-label="Select all" className={checkboxIcon}></span>
          </div>
        </div>
      );
    }
  };

  const className = classNames(option.className, 'p-multiselect-item', {
    'p-highlight': selected,
    'p-disabled': disabled
  });
  const checkboxClassName = classNames('p-checkbox-box p-component', {
    'p-highlight': selected,
    'p-disabled': disabled
  });
  const checkboxIcon = classNames('p-checkbox-icon p-c', { 'pi pi-check': selected });
  const content = template ? template(option) : label;

  return (
    <li
      aria-selected={selected}
      className={className}
      onClick={event => onClickEvent(event)}
      onKeyDown={event => onKeyDownEvent(event)}
      role="option"
      tabIndex={disabled ? null : tabIndex}>
      {renderCheckbox()}
      <label className={getClassNameLabel()}>{content}</label>
    </li>
  );
})
