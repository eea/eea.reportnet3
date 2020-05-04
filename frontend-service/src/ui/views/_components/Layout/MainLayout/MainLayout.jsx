import React, { Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Footer } from './_components';
import { Header } from './_components/Header';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { UserService } from 'core/services/User';

import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';

const MainLayout = ({ children }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notifications = useContext(NotificationContext);
  const themeContext = useContext(ThemeContext);
  const userContext = useContext(UserContext);

  const [margin, setMargin] = useState('50px');

  const getUserConfiguration = async () => {
    try {
      const userConfiguration = await UserService.getConfiguration();

      userContext.onChangeDateFormat(userConfiguration.dateFormat);
      userContext.onChangeRowsPerPage(parseInt(userConfiguration.rowsPerPage));
      userContext.onToggleLogoutConfirm(userConfiguration.showLogoutConfirmation);
      userContext.onToggleVisualTheme(userConfiguration.visualTheme);
      userContext.onUserFileUpload(userConfiguration.userImage);
      userContext.onToggleAmPm24hFormat(userConfiguration.amPm24h);
      themeContext.onToggleTheme(userConfiguration.visualTheme);
      userContext.onToggleSettingsLoaded(true);
    } catch (error) {
      console.error(error);
      userContext.onToggleSettingsLoaded(false);
      notifications.add({
        type: 'GET_CONFIGURATION_USER_SERVICE_ERROR'
      });
    }
  };

  useEffect(() => {
    if (!userContext.userProps.settingsLoaded) {
      getUserConfiguration();
    }
  }, []);

  useEffect(() => {
    async function fetchData() {
      if (isUndefined(userContext.id)) {
        try {
          const userObject = await UserService.refreshToken();
          userContext.onTokenRefresh(userObject);
        } catch (error) {
          notifications.add({
            key: 'TOKEN_REFRESH_ERROR',
            content: {}
          });
          await UserService.logout();
          userContext.onLogout();
        }
      }
    }
    fetchData();
    const bodySelector = document.querySelector('body');
    bodySelector.style.overflow = 'hidden auto';
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (leftSideBarContext.isLeftSideBarOpened) {
      setMargin('200px');
    } else {
      setMargin('50px');
    }
  }, [leftSideBarContext]);

  const onToggleSideBar = hover => {};

  useSocket();
  return (
    <div id={styles.mainLayoutContainer}>
      <Header />
      <div className={styles.mainContent} style={{ marginLeft: margin, transition: '0.5s' }}>
        <LeftSideBar onToggleSideBar={onToggleSideBar} />
        {children}
      </div>
      <Footer />
    </div>
  );
};
export { MainLayout };
