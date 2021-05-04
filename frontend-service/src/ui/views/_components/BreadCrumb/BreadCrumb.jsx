import { Fragment, useContext } from 'react';

import styles from './BreadCrumb.module.scss';

import isNil from 'lodash/isNil';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { isUndefined, isEmpty } from 'lodash';

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
    const className = !isUndefined(item.className) || !isEmpty(item.className) ? item.className : '';
    return (
      <li className={className} role="menuitem" style={item.style}>
        <a
          className="p-menuitem-link"
          href={item.href || ''}
          onClick={event => {
            event.preventDefault();
            onItemClick(event, item);
          }}
          target={item.target}>
          {!isNil(item.icon) && (
            <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons(item.icon)} />
          )}
          <span className="p-menuitem-text">{item.label ? item.label : <span className="srOnly">home</span>}</span>
        </a>
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
          <Fragment key={item.label + '_' + index}>
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
    return <li className="p-breadcrumb-chevron pi pi-chevron-right"></li>;
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
