import React, { Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Footer } from './_components';
import { Header } from './_components/Header';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { Notifications } from 'ui/views/_components/Notifications';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';

const MainLayout = ({ children, leftSideBarConfig }) => {
  const [margin, setMargin] = useState('50px');
  const notifications = useContext(NotificationContext);
  const user = useContext(UserContext);
  useEffect(() => {
    async function fetchData() {
      if (isUndefined(user.id)) {
        try {
          const userObject = await UserService.refreshToken();
          user.onTokenRefresh(userObject);
        } catch (error) {
          notifications.add({
            key: 'TOKEN_REFRESH_ERROR',
            content: {}
          });
          await UserService.logout();
          user.onLogout();
        }
      }
    }
    fetchData();
    const bodySelector = document.querySelector('body');
    bodySelector.style.overflow = 'hidden auto';
  }, []);

  const onToggleSideBar = hover => {
    if (hover) {
      setMargin('200px');
    } else {
      setMargin('50px');
    }
  };

  useSocket();
  return (
    <Fragment>
      {/* <Navigation /> */}
      {/* <div className={styles.disclaimer}>
        <span className="p-messages-icon pi  pi-info-circle"></span>
        {resources.messages['disclaimerTitle']}
      </div> */}
      <Header />
      <div className={styles.mainContent} style={{ marginLeft: margin, transition: '0.5s' }}>
        <LeftSideBar leftSideBarConfig={leftSideBarConfig} onToggleSideBar={onToggleSideBar} />
        {children}
      </div>
      <Footer />
    </Fragment>
  );
};
export { MainLayout };
