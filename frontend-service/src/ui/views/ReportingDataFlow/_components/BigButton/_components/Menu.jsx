import React, { useEffect } from 'react';
import style from '../BigButton.module.css';
import { Icon } from 'ui/views/_components/Icon';

const Menu = ({ model }) => {
  if (model) {
    return (
      <div className={`${style.dropDownMenu} p-menu-overlay-visible`} style={{ display: 'none' }}>
        <ul>
          {model ? (
            model.map((item, i) => (
              <li key={i}>
                <a
                  className={item.disabled ? style.menuItemDisabled : null}
                  onClick={e => {
                    e.preventDefault();
                    if (!item.disabled) item.command();
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
