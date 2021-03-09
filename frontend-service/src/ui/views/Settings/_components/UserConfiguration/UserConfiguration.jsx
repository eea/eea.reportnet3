import React, { Fragment, useContext } from 'react';
import styles from './userConfiguration.module.scss';

import DarkGray from 'assets/images/layers/DarkGray.png';
import Gray from 'assets/images/layers/Gray.png';
import Imagery from 'assets/images/layers/Imagery.png';
import ImageryClarity from 'assets/images/layers/ImageryClarity.png';
import ImageryFirefly from 'assets/images/layers/ImageryFirefly.png';
import NationalGeographic from 'assets/images/layers/NationalGeographic.png';
import Oceans from 'assets/images/layers/Oceans.png';
import ShadedRelief from 'assets/images/layers/ShadedRelief.png';
import Streets from 'assets/images/layers/Streets.png';
import Topographic from 'assets/images/layers/Topographic.png';

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

  const loadImage = layer => {
    switch (layer) {
      case 'Topographic':
        return Topographic;
      case 'Streets':
        return Streets;
      case 'National Geographic':
        return NationalGeographic;
      case 'Oceans':
        return Oceans;
      case 'Gray':
        return Gray;
      case 'Dark Gray':
        return DarkGray;
      case 'Imagery':
        return Imagery;
      case 'Imagery (Clarity)':
        return ImageryClarity;
      case 'Imagery (Firefly)':
        return ImageryFirefly;
      case 'Shaded Relief':
        return ShadedRelief;
      default:
        return '';
    }
  };

  const basemapTemplate = option => {
    if (!option.value) {
      return option.label;
    } else {
      return (
        <div className={`p-clearfix ${styles.basemapItem}`}>
          <span style={{ margin: '.5em .25em 0 0.5em' }}>{option.label}</span>
          <img alt={option.label} src={loadImage(option.label)} />
        </div>
      );
    }
  };

  const basemapLayerDropdown = (
    <Fragment>
      <Dropdown
        className={styles.basemapLayer}
        id={`basemapLayer`}
        itemTemplate={basemapTemplate}
        name="basemapLayer"
        // optionLabel="label"
        options={resources.userParameters['defaultBasemapLayer']}
        onChange={async e => {
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.basemapLayer = e.target.value;
          const response = await changeUserProperties(inmUserProperties);
          if (response.status >= 200 && response.status <= 299) {
            userContext.onChangeBasemapLayer(e.target.value);
          }
        }}
        placeholder="select"
        value={userContext.userProps.basemapLayer}
      />
      <label htmlFor="basemapLayer" className="srOnly">
        {resources.messages['userSettingsBasemapLayerSubtitle']}
      </label>
    </Fragment>
  );

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
    <Fragment>
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
    </Fragment>
  );

  const amPmSwitch = (
    <Fragment>
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
    </Fragment>
  );

  const confirmationLogoutSwitch = (
    <Fragment>
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
    </Fragment>
  );

  const chooseTypeViewSwitch = (
    <Fragment>
      <span className={styles.switchTextInput}>{`${resources.messages['magazineView']}`}</span>
      <InputSwitch
        checked={userContext.userProps.listView}
        onChange={async e => {
          userContext.onToggleTypeView(e.value);
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.listView = e.value;
          const response = await changeUserProperties(inmUserProperties);
          if (response.status < 200 || response.status > 299) {
            userContext.onToggleTypeView(!e.value);
          }
        }}
        tooltip={
          userContext.userProps.listView === true
            ? resources.messages['toggleMagazineView']
            : resources.messages['toggleListView']
        }
      />
      <span className={styles.switchTextInput}>{`${resources.messages['listView']}`}</span>
    </Fragment>
  );

  const rowsInPaginationDropdown = (
    <Fragment>
      <Dropdown
        id={`rowsPage`}
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
      <label htmlFor="rowsPage" className="srOnly">
        {resources.messages['userSettingsRowsPerPageSubtitle']}
      </label>
    </Fragment>
  );

  const dateFormatDropdown = (
    <Fragment>
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
    </Fragment>
  );

  const basemapLayerConfiguration = (
    <TitleWithItem
      title={resources.messages['basemapLayer']}
      icon="map"
      iconSize="2rem"
      subtitle={resources.messages['userSettingsBasemapLayerSubtitle']}
      items={[basemapLayerDropdown]}
    />
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

  const viewConfiguration = (
    <TitleWithItem
      title={resources.messages['userTypeOfView']}
      icon="eye"
      iconSize="2rem"
      subtitle={resources.messages['userTypeOfViewSubtitle']}
      items={[chooseTypeViewSwitch]}
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
        {viewConfiguration}
        {logoutConfiguration}
        {basemapLayerConfiguration}
      </div>
    </React.Fragment>
  );
};

export { UserConfiguration };
