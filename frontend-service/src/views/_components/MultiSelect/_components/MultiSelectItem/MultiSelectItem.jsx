import classNames from 'classnames';

export const MultiSelectItem = ({
  disabled = false,
  label,
  onClick,
  onKeyDown,
  option,
  selected,
  tabIndex,
  template
}) => {
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
      // id={label}
      onClick={event => onClickEvent(event)}
      onKeyDown={event => onKeyDownEvent(event)}
      role="option"
      tabIndex={disabled ? null : tabIndex}>
      {!disabled && (
        <div className="p-checkbox p-component">
          <div className={checkboxClassName}>
            <span aria-label="Select all" className={checkboxIcon}></span>
          </div>
        </div>
      )}
      <label>{content}</label>
    </li>
  );
};
