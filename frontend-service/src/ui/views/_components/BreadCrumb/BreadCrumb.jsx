import React from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import './BreadCrumb.module.css';

export const BreadCrumb = ({ className, id, model, style }) => {
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
    const className = (item.className, { 'p-disabled': item.disabled });

    return (
      <li role="menuitem" className={className} style={item.style}>
        <a className="p-menuitem-link" target={item.target} onMouseDown={event => onItemClick(event, item)}>
          <FontAwesomeIcon icon={AwesomeIcons(item.icon)} />
          <span className="p-menuitem-text">{item.label}</span>
        </a>
      </li>
    );
  };

  const onLoadModel = () => {
    if (model) {
      const items = model.map((item, index) => {
        const menuitem = onLoadItem(item);
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
    <div id={id} className={`p-breadcrumb p-component, ${className}`} style={style}>
      <ul>{onLoadModel()}</ul>
    </div>
  );
};
