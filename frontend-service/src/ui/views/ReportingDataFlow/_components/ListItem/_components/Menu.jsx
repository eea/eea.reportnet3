import React, { useEffect } from 'react';
import style from '../ListItem.module.css';
import { Icon } from 'ui/views/_components/Icon';

const Menu = ({ model }) => {
  if (model) {
    return (
      <div className={`${style.dropDownMenu} p-menu-overlay-visible`} style={{ display: 'none' }}>
        <ul>
          {model ? (
            model.map(item => (
              <li>
                <a
                  className={item.disabled ? style.menuItemDisabled : null}
                  onClick={e => {
                    e.preventDefault();
                    item.command();
                  }}
                  disabled={item.disabled}>
                  <Icon icon={item.icon} />
                  {item.label}
                </a>
              </li>
            ))
          ) : (
            <li></li>
          )}
        </ul>
      </div>
    );
  } else {
    return <></>;
  }
};

export { Menu };
