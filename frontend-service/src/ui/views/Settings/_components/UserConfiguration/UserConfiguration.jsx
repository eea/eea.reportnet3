import React, { useContext } from 'react';
import styles from './userConfiguration.module.scss';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { TitleWithItem } from 'ui/views/_components/TitleWithItem';

import { UserService } from 'core/services/User';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserConfiguration = () => {
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const changeUserProperties = async userProperties => {
    try {
      const response = await UserService.updateAttributes(userProperties);
      return response;
    } catch (error) {
      notificationContext.add({
        type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR'
      });
    }
  };

  const themeSwitch = (
    <React.Fragment>
      <span className={styles.switchTextInput}>{resources.messages['light']}</span>
      <InputSwitch
        checked={userContext.userProps.visualTheme === 'dark'}
        onChange={async e => {
          themeContext.onToggleTheme(e.value ? 'dark' : 'light');
          userContext.onToggleVisualTheme(e.value ? 'dark' : 'light');
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.visualTheme = e.value ? 'dark' : 'light';
          const response = await changeUserProperties(inmUserProperties);
          if (response.status < 200 || response.status > 299) {
            themeContext.onToggleTheme(!e.value ? 'dark' : 'light');
            userContext.onToggleVisualTheme(!e.value ? 'dark' : 'light');
          }
        }}
        sliderCheckedClassName={styles.themeSwitcherInputSwitch}
        tooltip={
          userContext.userProps.visualTheme === 'light'
            ? resources.messages['toggleDarkTheme']
            : resources.messages['toggleLightTheme']
        }
        tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
      />
      <span className={styles.switchTextInput}>{resources.messages['dark']}</span>
    </React.Fragment>
  );

  const amPmSwitch = (
    <React.Fragment>
      <span className={styles.switchTextInput}>AM/PM</span>
      <InputSwitch
        checked={userContext.userProps.amPm24h}
        onChange={async e => {
          userContext.onToggleAmPm24hFormat(e.value);
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.amPm24h = e.value;
          const response = await changeUserProperties(inmUserProperties);
          if (response.status < 200 || response.status > 299) {
            userContext.onToggleAmPm24hFormat(!e.value);
          }
        }}
        tooltip={
          userContext.userProps.amPm24h === true ? resources.messages['amPmFormat'] : resources.messages['24hFormat']
        }
      />
      <span className={styles.switchTextInput}>24H</span>
    </React.Fragment>
  );

  const confirmationLogoutSwitch = (
    <React.Fragment>
      <span className={styles.switchTextInput}>No popup</span>
      <InputSwitch
        checked={userContext.userProps.showLogoutConfirmation}
        onChange={async e => {
          userContext.onToggleLogoutConfirm(e.value);
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.showLogoutConfirmation = e.value;
          const response = await changeUserProperties(inmUserProperties);
          if (response.status < 200 || response.status > 299) {
            userContext.onToggleLogoutConfirm(!e.value);
          }
        }}
        tooltip={
          userContext.userProps.showLogoutConfirmation === true
            ? resources.messages['toggleConfirmationOff']
            : resources.messages['toggleConfirmationOn']
        }
      />
      <span className={styles.switchTextInput}>Popup</span>
    </React.Fragment>
  );

  const rowsInPaginationDropdown = (
    <React.Fragment>
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
    </React.Fragment>
  );

  const dateFormatDropdown = (
    <React.Fragment>
      <Dropdown
        name="dateFormat"
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
    </React.Fragment>
  );

  const themeConfiguration = (
    <TitleWithItem
      title={resources.messages['theme']}
      icon="palette"
      iconSize="2rem"
      subtitle={resources.messages['userSettingsThemeSubtitle']}
      items={[themeSwitch]}
    />
  );

  const logoutConfiguration = (
    <TitleWithItem
      title={resources.messages['userConfirmationLogout']}
      icon="power-off"
      iconSize="2rem"
      subtitle={resources.messages['userSettingsConfirmSubtitle']}
      items={[confirmationLogoutSwitch]}
    />
  );

  const rowsInPaginationConfiguration = (
    <TitleWithItem
      title={resources.messages['userRowsInPagination']}
      icon="list-ol"
      iconSize="2rem"
      subtitle={resources.messages['userSettingsRowsPerPageSubtitle']}
      items={[rowsInPaginationDropdown]}
    />
  );

  const dateFormatSubtitle = (
    <React.Fragment>
      <div>{resources.messages['dateFormatSubtitle']}</div>
      <div className={styles.dateFormatWarning}>{resources.messages['dateFormatWarning']}</div>
    </React.Fragment>
  );

  const dateFormatConfiguration = (
    <TitleWithItem
      title={resources.messages['dateFormat']}
      icon="calendar"
      iconSize="2rem"
      subtitle={dateFormatSubtitle}
      items={[dateFormatDropdown, amPmSwitch]}
    />
  );

  return (
    <React.Fragment>
      <div className={styles.userConfiguration}>
        {rowsInPaginationConfiguration}
        {dateFormatConfiguration}
        {themeConfiguration}
        {logoutConfiguration}
      </div>
    </React.Fragment>
  );
};

export { UserConfiguration };
