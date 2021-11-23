import React, { useRef } from 'react';
import { Fragment } from 'react';
import ReactTooltip from 'react-tooltip';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isNil from 'lodash/isNil';

import styles from './MenuItem.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

export const MenuItem = ({ item }) => {
  const ref = useRef(null);

  const isEllipsisActive = () => ref.current?.offsetWidth < ref.current?.scrollWidth;

  return (
    <Fragment>
      <li className={'p-menuitem'} data-for={item.label} data-tip>
        <span
          className={`p-menuitem-link ${item.disabled ? 'p-disabled' : null} ${styles.menuItem} `}
          disabled={item.disabled}
          onClick={e => {
            e.preventDefault();
            if (!item.disabled) {
              item.command();
            }
          }}
          ref={ref}>
          {!isNil(item.icon) && <FontAwesomeIcon icon={AwesomeIcons(item.icon)} role="presentation" />}
          <span>{item.label}</span>
        </span>
      </li>

      {isEllipsisActive() && (
        <ReactTooltip className={styles.tooltip} effect="solid" id={item.label} place="right">
          {item.label}
        </ReactTooltip>
      )}
    </Fragment>
  );
};
