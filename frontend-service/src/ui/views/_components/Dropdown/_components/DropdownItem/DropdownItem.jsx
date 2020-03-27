import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { isUndefined, isNull } from 'util';
import uuid from 'uuid';

export const DropdownItem = ({
  option = null,
  label = null,
  template = null,
  selected = false,
  disabled = false,
  onClick = null
}) => {
  let classNamed = classNames('p-dropdown-item', {
    'p-highlight': selected,
    'p-disabled': disabled,
    'p-dropdown-item-empty': !label || label.length === 0
  });
  let content = template ? template(option) : label;

  const onItemClick = event => {
    if (!isUndefined(onClick) && !isNull(onClick)) {
      onClick({
        originalEvent: event,
        option: option
      });
    }
  };

  return (
    <li className={classNamed} onClick={onItemClick} key={uuid.v4()}>
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
