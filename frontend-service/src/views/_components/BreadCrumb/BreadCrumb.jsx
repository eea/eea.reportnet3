import { Fragment, useContext } from 'react';

import styles from './BreadCrumb.module.scss';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { BreadCrumbContext } from 'views/_functions/Contexts/BreadCrumbContext';

export const BreadCrumb = ({ className, id, style, isPublic = false }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const onItemClick = (event, item) => {
    if (item.disabled) {
      event.preventDefault();
      return;
    }

    if (item.href) {
      event.preventDefault();
    }

    if (item.command) {
      item.command({
        originalEvent: event,
        item: item
      });
    }
  };

  const onLoadItem = item => {
    const className = !isUndefined(item.className) || !isEmpty(item.className) ? item.className : null;
    return (
      <li className={className} role="menuitem" style={item.style}>
        <span
          className="p-menuitem-link"
          onClick={event => {
            event.preventDefault();
            onItemClick(event, item);
          }}
          target={item.target}>
          {!isNil(item.icon) && (
            <FontAwesomeIcon
              aria-hidden={false}
              aria-label={item.label}
              className="p-breadcrumb-home"
              icon={AwesomeIcons(item.icon)}
              role="button"
            />
          )}
          <span className="p-menuitem-text">{item.label ? item.label : <span className="srOnly">Home</span>}</span>
        </span>
      </li>
    );
  };

  const onLoadModel = () => {
    const { model } = breadCrumbContext;
    if (model) {
      const items = model.map((item, index) => {
        const menuitem = onLoadItem(item, index);
        const separator = index === model.length - 1 ? null : onLoadSeparator();

        return (
          <Fragment key={item.label}>
            {menuitem}
            {separator}
          </Fragment>
        );
      });

      return items;
    } else {
      return null;
    }
  };

  const onLoadSeparator = () => {
    return <li className="p-breadcrumb-chevron pi pi-chevron-right" role="presentation"></li>;
  };

  return (
    <div
      className={`${styles.container} ${isPublic ? styles.isPublic : ''} ${className ? className : ''}`}
      id={id}
      style={style}>
      <ul>{onLoadModel()}</ul>
    </div>
  );
};
