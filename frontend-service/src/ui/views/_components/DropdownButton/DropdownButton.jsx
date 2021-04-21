import { useRef } from 'react';

import styles from './DropdownButton.module.scss';

import { DropDownMenu } from './_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';

const DropdownButton = ({ children, icon, model, buttonStyle, iconStyle, disabled }) => {
  const menuRef = useRef();
  return (
    <div className={styles.dropDownWrapper} style={buttonStyle}>
      <div
        className={!disabled ? styles.dropdown : null}
        onClick={e => {
          if (!disabled) menuRef.current.show(e);
        }}>
        <Icon icon={icon} style={iconStyle ? iconStyle : { fontSize: '1.5rem' }} />
        {children}
      </div>
      <DropDownMenu ref={menuRef} model={model} />
    </div>
  );
};

export { DropdownButton };
