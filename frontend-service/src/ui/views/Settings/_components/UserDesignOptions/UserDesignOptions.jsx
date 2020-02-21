import React, { useContext } from 'react';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import styles from './UserDesignOptions.module.scss';

const UserDesignOptions = () => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  return (
    <div className={styles.userDesignContainer}>
      <h3 className={styles.userThemeTitle}>{resources.messages['defaultVisualTheme']}</h3>
      <Dropdown
        className={styles.dropdownFieldType}
        options={[
          { label: 'light', value: 'light' },
          { label: 'dark', value: 'dark' }
        ]}
        onChange={e => {}}
        placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
        value="light"
      />
    </div>
  );
};

export { UserDesignOptions };
