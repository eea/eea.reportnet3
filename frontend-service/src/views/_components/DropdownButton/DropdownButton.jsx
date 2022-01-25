import { useRef } from 'react';

import styles from './DropdownButton.module.scss';

import { DropDownMenu } from './_components/DropDownMenu';
import { Icon } from 'views/_components/Icon';

export const DropdownButton = ({ children, icon, model, buttonStyle, iconStyle, disabled }) => {
  const menuRef = useRef();
  return (
    <div className={styles.dropDownWrapper} style={buttonStyle}>
      <div
        className={!disabled ? styles.dropdown : null}
        onClick={e => {
          if (!disabled) {
            menuRef.current.show(e);
          }
        }}>
        <Icon icon={icon} style={iconStyle ? iconStyle : { fontSize: '1.5rem' }} />
        {children}
      </div>
      <DropDownMenu model={model} ref={menuRef} />
    </div>
  );
};
