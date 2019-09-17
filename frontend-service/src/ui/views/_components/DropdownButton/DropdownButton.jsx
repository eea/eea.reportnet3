import React, { useState } from 'react';

import styles from './DropdownButton.module.scss';

import { config } from 'conf';

import { Icon } from 'ui/views/_components/Icon';

const DropdownButton = ({ children, icon, model, hasWritePermissions }) => {
  const [isVisible, setisVisible] = useState(false);
  const toggleMenu = () => {
    setisVisible(!isVisible);
  };
  return (
    <div className={styles.dropdown} onClick={e => toggleMenu()}>
      <Icon icon={icon} style={{ fontSize: '1.5rem' }} />
      {children}
      <div
        className={`${styles.dropdown_content} p-menu p-menu-dynamic p-menu-overlay p-component p-menu-overlay-visible`}
        style={{ display: isVisible ? 'block' : 'none' }}>
        <ul className="p-menu-list p-reset">
          {model.map(item => {
            if (!item.show) return <></>;
            return (
              <li className="p-menuitem" role="menuitem" key={item.label}>
                <div className="p-menuitem-link" onClick={item.menuItemFunction}>
                  <span className={`p-menuitem-icon ${config.icons[item.icon]}`} />
                  <span className="p-menuitem-text">{item.label}</span>
                </div>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
};

export { DropdownButton };
