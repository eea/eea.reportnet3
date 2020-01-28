import React, { Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Footer } from './_components';
import { Header } from './_components/Header';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { UserService } from 'core/services/User';

import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';

const MainLayout = ({ children, leftSideBarConfig }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const notifications = useContext(NotificationContext);
  const user = useContext(UserContext);

  const [margin, setMargin] = useState('50px');

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
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (breadCrumbContext.isLeftSideBarOpened) {
      setMargin('200px');
    } else {
      setMargin('50px');
    }
  }, [breadCrumbContext]);

  const onToggleSideBar = hover => {};

  useSocket();
  return (
    <Fragment>
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
