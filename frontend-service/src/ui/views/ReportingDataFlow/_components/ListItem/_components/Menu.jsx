import React, { useEffect } from 'react';
import style from '../ListItem.module.css';
import { Icon } from 'ui/views/_components/Icon';

const Menu = ({ dropdownState, model, dropdowndispatch }) => {
  if (model) {
    return (
      <div className={style.dropDownMenu} hidden={dropdownState.hidden}>
        <ul>
          {model ? (
            model.map(item => (
              <li>
                <a
                  className={item.disabled ? style.menuItemDisabled : null}
                  onClick={e => {
                    e.preventDefault();
                    item.command();
                    dropdowndispatch({ type: 'TOGGLE_MENU', menu: null });
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
