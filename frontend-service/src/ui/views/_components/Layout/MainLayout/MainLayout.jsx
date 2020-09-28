import React, { useContext, useEffect, useState } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './MainLayout.module.css';

import { EuFooter } from './_components/EuFooter';
import { Footer } from './_components';
import { Header } from './_components/Header';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { NotificationsList } from './_components/NotificationsList';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { UserService } from 'core/services/User';

import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';

const MainLayout = ({ children, isPublic = false }) => {
  const element = document.compatMode === 'CSS1Compat' ? document.documentElement : document.body;
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notifications = useContext(NotificationContext);
  const themeContext = useContext(ThemeContext);
  const userContext = useContext(UserContext);

  const [isNotificationVisible, setIsNotificationVisible] = useState(false);
  const [margin, setMargin] = useState('50px');
  const [mainContentStyle, setMainContentStyle] = useState({
    height: `auto`,
    minHeight: `${window.innerHeight - 180}px`,
    marginTop: '180px'
  });
  const [pageContentStyle, setPageContentStyle] = useState({
    maxWidth: `${element.clientWidth - 50}px`
  });

  useEffect(() => {
    window.addEventListener('resize', calculateMainContentWidth);
    return () => {
      window.removeEventListener('resize', calculateMainContentWidth);
    };
  });

  const calculateMainContentWidth = () => {
    const clientWidth = element.clientWidth;

    const maxWidth = leftSideBarContext.isLeftSideBarOpened
      ? { maxWidth: `${clientWidth - 200}px` }
      : { maxWidth: `${clientWidth - 50}px` };

    setPageContentStyle({ ...maxWidth });
  };

  useEffect(() => {
    calculateMainContentWidth();
  }, [leftSideBarContext.isLeftSideBarOpened]);

  const getUserConfiguration = async () => {
    try {
      const userConfiguration = await UserService.getConfiguration();
      userContext.onChangeBasemapLayer(userConfiguration.basemapLayer);
      userContext.onChangeDateFormat(userConfiguration.dateFormat);
      userContext.onChangeRowsPerPage(parseInt(userConfiguration.rowsPerPage));
      userContext.onToggleLogoutConfirm(userConfiguration.showLogoutConfirmation);
      userContext.onToggleVisualTheme(userConfiguration.visualTheme);
      userContext.onUserFileUpload(userConfiguration.userImage);
      userContext.onToggleAmPm24hFormat(userConfiguration.amPm24h);
      themeContext.onToggleTheme(userConfiguration.visualTheme);
      userContext.onToggleSettingsLoaded(true);
      userContext.onToggleTypeView(userConfiguration.listView);
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
  }, [leftSideBarContext.isLeftSideBarOpened]);

  const onToggleSideBar = hover => {};

  const onMainContentStyleChange = updatedMainContentStyle => {
    const newMainContentStyle = { ...mainContentStyle, ...updatedMainContentStyle };

    setMainContentStyle(newMainContentStyle);
  };

  useSocket();
  return (
    <div id={styles.mainLayoutContainer}>
      {isNotificationVisible && (
        <NotificationsList
          isNotificationVisible={isNotificationVisible}
          setIsNotificationVisible={setIsNotificationVisible}
        />
      )}
      <Header isPublic={isPublic} onMainContentStyleChange={onMainContentStyleChange} />
      <div id="mainContent" className={styles.mainContent} style={mainContentStyle}>
        <LeftSideBar onToggleSideBar={onToggleSideBar} setIsNotificationVisible={setIsNotificationVisible} />
        <div id="pageContent" className={styles.pageContent} style={pageContentStyle}>
          {children}
        </div>
      </div>

      <Footer leftMargin={margin} />
      <EuFooter leftMargin={margin} />
    </div>
  );
};
export { MainLayout };
