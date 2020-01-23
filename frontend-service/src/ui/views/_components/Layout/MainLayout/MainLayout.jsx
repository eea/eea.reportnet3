import React, { Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Footer } from './_components';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { Navigation } from './_components';
import { Notifications } from 'ui/views/_components/Notifications';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';
import { BreadCrumb } from '../../BreadCrumb/BreadCrumb';

const MainLayout = ({ children, leftSideBarConfig }) => {
  const [margin, setMargin] = useState('50px');
  const notifications = useContext(NotificationContext);
  const breadCrumbContext = useContext(BreadCrumbContext);
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
      <Notifications />
      {/* <div className={styles.disclaimer}>
        <span className="p-messages-icon pi  pi-info-circle"></span>
        {resources.messages['disclaimerTitle']}
      </div> */}
      <div className={styles.mainContent} style={{ marginLeft: margin, transition: '0.5s' }}>
        <LeftSideBar leftSideBarConfig={leftSideBarConfig} onToggleSideBar={onToggleSideBar} />
        <BreadCrumb model={breadCrumbContext.model} />
        {children}
      </div>
      <Footer />
    </Fragment>
  );
};
export { MainLayout };
