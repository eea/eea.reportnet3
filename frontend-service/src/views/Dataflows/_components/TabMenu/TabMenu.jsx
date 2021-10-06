import React, { Fragment, useState } from 'react';
import PropTypes from 'prop-types';

import isNil from 'lodash/isNil';

import styles from './TabMenu.module.scss';

import classNames from 'classnames';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const TabMenu = ({
  activeIndex,
  className,
  headerLabelChildrenCount = {},
  headerLabelLoading = false,
  id,
  model,
  onTabChange,
  style
}) => {
  const [activeIndexTab, setActiveIndexTab] = useState(activeIndex);

  const getActiveIndex = () => (onTabChange ? activeIndex : activeIndexTab);

  const isSelected = index => index === getActiveIndex() || 0;

  const itemClick = (event, item, index) => {
    if (item.disabled) {
      event.preventDefault();
      return;
    }

    if (item.command) {
      item.command({ originalEvent: event, item: item });
    }

    if (!item.url) {
      event.preventDefault();
    }

    if (onTabChange) {
      onTabChange({ originalEvent: event, value: item, index });
    } else {
      setActiveIndexTab(index);
    }
  };

  const renderMenuItem = (item, index) => {
    const isActive = isSelected(index);
    const menuItemClassName = classNames('p-tabmenuitem', { 'p-highlight': isActive, 'p-disabled': item.disabled });

    return (
      <li
        aria-disabled={item.disabled}
        aria-expanded={isActive}
        aria-selected={isActive}
        className={`${menuItemClassName} ${item.className || ''}`}
        key={item.id}
        role="tab"
        style={item.style}>
        <a
          className="p-menuitem-link"
          href={item.url || '#'}
          onClick={event => itemClick(event, item, index)}
          role="presentation"
          target={item.target}>
          {renderMenuItemLabel(item, index)}
        </a>
      </li>
    );
  };

  const renderMenuItemLabel = (item, index) => {
    let label = item.label;
    if (!isNil(headerLabelChildrenCount[item.id])) {
      if (headerLabelLoading[item.id] && isSelected(index)) {
        label = (
          <Fragment>
            {item.label} (
            <span>
              <FontAwesomeIcon className={`${styles.icon} ${styles.spinner}`} icon={AwesomeIcons('spinner')} />
            </span>
            )
          </Fragment>
        );
      } else {
        label = (
          <Fragment>
            {item.label} ({headerLabelChildrenCount[item.id]})
          </Fragment>
        );
      }
    } else {
      label = item.label;
    }

    return (
      <div>
        {!isNil(item.icon) ? <span className={classNames('p-menuitem-icon', item.icon)}></span> : null}
        {!isNil(item.label) ? <span className="p-menuitem-text">{label}</span> : null}
      </div>
    );
  };

  // MODEL PARAMS
  // className command disabled icon label style target url

  return (
    <div className={classNames('p-tabmenu p-component', className)} id={id} style={style}>
      <ul className="p-tabmenu-nav p-reset" role="tablist">
        {model.map((item, index) => renderMenuItem(item, index))}
      </ul>
    </div>
  );
};

TabMenu.defaultProps = { activeIndex: 0, className: '', id: '', model: [], onTabChange: null, style: {} };

TabMenu.propTypes = {
  activeIndex: PropTypes.number,
  className: PropTypes.string,
  id: PropTypes.string,
  model: PropTypes.array,
  onTabChange: PropTypes.func,
  style: PropTypes.object
};
