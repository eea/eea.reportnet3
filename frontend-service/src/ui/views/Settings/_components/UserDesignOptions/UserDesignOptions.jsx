import React, { useContext } from 'react';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import styles from './UserDesignOptions.module.scss';

const UserDesignOptions = () => {
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);
  const userContext = useContext(UserContext);

  return (
    <div className={styles.userDesignContainer}>
      <h3 className={styles.userThemeTitle}>{resources.messages['defaultVisualTheme']}</h3>
      <Dropdown
        name="visualTheme"
        className={styles.dropdownFieldType}
        options={resources.userParameters['visualTheme']}
        onChange={e => {
          themeContext.onToggleTheme(e.value);
          userContext.defaultVisualTheme(e.value);
        }}
        placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
        value={themeContext.currentTheme}
      />
    </div>
  );
};

export { UserDesignOptions };
