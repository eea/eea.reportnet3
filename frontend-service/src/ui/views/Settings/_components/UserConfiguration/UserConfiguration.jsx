import React, { useContext } from 'react';
import styles from './userConfiguration.module.scss';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { UserService } from 'core/services/User';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserConfiguration = props => {
  const userContext = useContext(UserContext);
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const changeUserProperties = async userProperties => {
    console.log(userProperties);
    try {
      const response = await UserService.updateAttributes(userProperties);
      return response;
    } catch (error) {
      console.error(error);
      //Notification
    }
  };

  return (
    <div className={styles.userConfigurationContainer}>
      <div className={styles.userConfirmLogout}>
        <h3>{resources.messages['userThemeSelection']}</h3>
        <Dropdown
          name="visualTheme"
          options={resources.userParameters['visualTheme']}
          onChange={async e => {
            const inmUserProperties = { ...userContext.userProps };
            inmUserProperties.defaultVisualTheme = e.value;
            const response = await changeUserProperties(inmUserProperties);
            console.log({ response });
            if (response.status >= 200 && response.status <= 299) {
              themeContext.onToggleTheme(e.value);
              userContext.defaultVisualTheme(e.value);
            }
          }}
          placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
          value={userContext.userProps.defaultVisualTheme}
        />
      </div>
      <div className={styles.userConfirmLogout}>
        <div>
          <h3>{resources.messages['userConfirmationLogout']}</h3>
          <InputSwitch
            checked={userContext.userProps.showLogoutConfirmation}
            style={{ marginRight: '1rem' }}
            onChange={async e => {
              const inmUserProperties = { ...userContext.userProps };
              inmUserProperties.showLogoutConfirmation = e.value;
              const response = await changeUserProperties(inmUserProperties);
              if (response.status >= 200 && response.status <= 299) {
                userContext.onToggleLogoutConfirm(e.value);
              }
            }}
            tooltip={
              userContext.userProps.showLogoutConfirmation === true
                ? resources.messages['toogleConfirmationOff']
                : resources.messages['toogleConfirmationOn']
            }
          />
        </div>
      </div>

      <div className={styles.userConfirmLogout}>
        <h3>{resources.messages['userDefaultRowsPage']}</h3>
        <Dropdown
          name="rowPerPage"
          options={resources.userParameters['defaultRowsPage']}
          onChange={async e => {
            const inmUserProperties = { ...userContext.userProps };
            inmUserProperties.defaultRowSelected = e.target.value;
            const response = await changeUserProperties(inmUserProperties);
            if (response.status >= 200 && response.status <= 299) {
              userContext.defaultRowSelected(e.target.value);
            }
          }}
          placeholder="select"
          value={userContext.userProps.defaultRowSelected}
        />
      </div>
      <div className={styles.userConfirmLogout}>
        <h3>{resources.messages['dateFormat']}</h3>
        <h5 className={styles.italicTitle}>{resources.messages['dateFormatWarning']}</h5>
        <Dropdown
          name="rowPerPage"
          options={resources.userParameters['dateFormat']}
          onChange={async e => {
            const inmUserProperties = { ...userContext.userProps };
            inmUserProperties.dateFormat = e.target.value;
            const response = await changeUserProperties(inmUserProperties);
            console.log({ response });
            if (response.status >= 200 && response.status <= 299) {
              userContext.dateFormat(e.target.value);
            }
          }}
          placeholder="select"
          value={userContext.userProps.dateFormat}
        />
      </div>
    </div>
  );
};

export { UserConfiguration };
