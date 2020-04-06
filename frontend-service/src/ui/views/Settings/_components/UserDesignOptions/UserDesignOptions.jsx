import React, { useContext } from 'react';
import styles from './UserDesignOptions.module.scss';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { UserImgUpload } from './_components/UserImg/_components/UserImgUpload';
import { isUndefined } from 'lodash';
const UserDesignOptions = props => {
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);
  const userContext = useContext(UserContext);

  return (
    <React.Fragment>
      <div className={styles.userDesignContainer}>
        <h3 className={styles.userThemeTitle}>{resources.messages['userThemeSelection']}</h3>
        <Dropdown
          name="visualTheme"
          className={styles.dropdownFieldType}
          options={resources.userParameters['visualTheme']}
          onChange={e => {
            themeContext.onToggleTheme(e.value);
            userContext.defaultVisualTheme(e.value);
          }}
          placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
          value={userContext.userProps.defaultVisualTheme}
        />

        {/* !isUndefined(props.Attributes.defaultVisualTheme)
            ? themeContext.currentTheme
            : props.Attributes.defaultVisualTheme */}
        <div className={styles.userUploadImg}>
          <h3 className={styles.userThemeTitle}>{resources.messages['userSelectImage']}</h3>

          <UserImgUpload />
        </div>
      </div>
    </React.Fragment>
  );
};

export { UserDesignOptions };
