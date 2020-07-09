import React, { useContext } from 'react';

import styles from './BreadCrumb.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';

export const BreadCrumb = ({ className, id, style }) => {
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
    const className = '';

    return (
      <li role="menuitem" className={className} style={item.style}>
        <a
          href={item.href || ''}
          className="p-menuitem-link"
          target={item.target}
          onClick={event => {
            event.preventDefault();
            onItemClick(event, item);
          }}>
          <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons(item.icon)} />
          <span className="p-menuitem-text">{item.label}</span>
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
          <React.Fragment key={item.label + '_' + index}>
            {menuitem}
            {separator}
          </React.Fragment>
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
    <div id={id} className={`${styles.container} ${className ? className : ''}`} style={style}>
      <ul>{onLoadModel()}</ul>
    </div>
  );
};
