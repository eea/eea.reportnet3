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
        <div className={styles.switchDiv}>
          <span className={styles.switchTextInput}>{resources.messages['light']}</span>
          <InputSwitch
            checked={userContext.userProps.visualTheme === 'dark'}
            onChange={async e => {
              const inmUserProperties = { ...userContext.userProps };
              inmUserProperties.visualTheme = e.value ? 'dark' : 'light';
              const response = await changeUserProperties(inmUserProperties);
              if (response.status >= 200 && response.status <= 299) {
                console.log(e.value);
                themeContext.onToggleTheme(e.value ? 'dark' : 'light');
                userContext.onToggleVisualTheme(e.value ? 'dark' : 'light');
              }
            }}
            sliderCheckedClassName={styles.themeSwitcherInputSwitch}
            style={{ marginRight: '1rem' }}
            tooltip={
              userContext.userProps.visualTheme === 'light'
                ? resources.messages['toggleDarkTheme']
                : resources.messages['toggleLightTheme']
            }
            tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
          />
          <span className={styles.switchTextInput}>{resources.messages['dark']}</span>
        </div>
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
            inmUserProperties.rowsPerPage = e.target.value;
            const response = await changeUserProperties(inmUserProperties);
            if (response.status >= 200 && response.status <= 299) {
              userContext.onChangeRowsPerPage(e.target.value);
            }
          }}
          placeholder="select"
          value={userContext.userProps.rowsPerPage}
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
            if (response.status >= 200 && response.status <= 299) {
              userContext.onChangeDateFormat(e.target.value);
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
