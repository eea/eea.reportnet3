import React, { Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import isEmpty from 'lodash/isEmpty';

import { Footer } from './_components';
import { Header } from './_components/Header';
import { EuFooter } from './_components/EuFooter';
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
  const [leftSideBarStyle, setLeftSideBarStyle] = useState({});
  const [mainContentStyle, setMainContentStyle] = useState({});

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
    const header = document.getElementById('header');
    const mainContent = document.getElementById('mainContent');
    const pageContent = document.getElementById('pageContent');

    if (isEmpty(mainContentStyle) && header.clientHeight + mainContent.clientHeight < window.innerHeight) {
      setMainContentStyle({
        height: `${window.innerHeight - header.clientHeight}px`
      });
    } else if (
      !isEmpty(mainContentStyle) &&
      header.clientHeight + pageContent.children[0].clientHeight > window.innerHeight
    ) {
      setMainContentStyle({
        height: 'auto'
      });
    }
  }, [children]);

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

  const onLeftSideBarStyleChange = sideBarStyle => {
    setLeftSideBarStyle(sideBarStyle);
  };

  useSocket();
  return (
    <div id={styles.mainLayoutContainer}>
      <Header onLeftSideBarStyleChange={onLeftSideBarStyleChange} />
      <div id="mainContent" className={styles.mainContent} style={mainContentStyle}>
        <LeftSideBar onToggleSideBar={onToggleSideBar} style={leftSideBarStyle} style={leftSideBarStyle} />
        <div id="pageContent" className={styles.pageContent}>
          {children}
        </div>
      </div>
      <Footer leftMargin={margin} />
      <EuFooter leftMargin={margin} />
    </div>
  );
};
export { MainLayout };
