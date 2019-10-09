import React, { useRef } from 'react';

import styles from './DropdownButton.module.scss';

import { config } from 'conf';

import { DropDownMenu } from './_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';

const DropdownButton = ({ children, icon, model, hasWritePermissions }) => {
  const menuRef = useRef();
  return (
    <div className={styles.dropDownWrapper}>
      <div className={styles.dropdown} onClick={e => menuRef.current.show(e)}>
        <Icon icon={icon} style={{ fontSize: '1.5rem' }} />
        {children}
      </div>
      <DropDownMenu ref={menuRef} model={model} />
    </div>
  );
};

export { DropdownButton };
