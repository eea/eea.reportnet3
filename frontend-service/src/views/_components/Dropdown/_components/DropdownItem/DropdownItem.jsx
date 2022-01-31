import PropTypes from 'prop-types';
import classNames from 'classnames';
import isNil from 'lodash/isNil';

export const DropdownItem = ({
  option = null,
  label = null,
  template = null,
  selected = false,
  disabled = false,
  onClick = null
}) => {
  const classNamed = classNames('p-dropdown-item', {
    'p-highlight': selected,
    'p-disabled': disabled,
    'p-dropdown-item-empty': !label || label.length === 0
  });
  const content = template ? template(option) : label;

  const onItemClick = event => {
    if (!isNil(onClick)) {
      onClick({ originalEvent: event, option: option });
    }
  };

  return (
    <li className={classNamed} key={`_${option.label}`} onClick={onItemClick} onMouseDown={onItemClick}>
      {content}
    </li>
  );
};

DropdownItem.propTypes = {
  option: PropTypes.object,
  label: PropTypes.any,
  template: PropTypes.func,
  selected: PropTypes.bool,
  disabled: PropTypes.bool,
  onClick: PropTypes.func
};
