import React, { Fragment, useContext, useEffect } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Navigation } from './_components';
import { Footer } from './_components';
import { Notifications } from 'ui/views/_components/Notifications';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { useSocket } from 'ui/views/_components/Layout/MainLayout/_hooks';

const MainLayout = ({ children }) => {
  const user = useContext(UserContext);
  useEffect(() => {
    async function fetchData() {
      if (isUndefined(user.id)) {
        try {
          const userObject = await UserService.refreshToken();
          user.onTokenRefresh(userObject);
        } catch (error) {
          await UserService.logout();
          user.onLogout();
        }
      }
    }
    fetchData();
    const bodySelector = document.querySelector('body');
    bodySelector.style.overflow = 'hidden auto';
  }, []);
  useSocket();
  return (
    <Fragment>
      <Navigation />
      <Notifications />
      {/* <div className={styles.disclaimer}>
        <span className="p-messages-icon pi  pi-info-circle"></span>
        {resources.messages['disclaimerTitle']}
      </div> */}
      <div className={styles.mainContent}>{children}</div>
      <Footer />
    </Fragment>
  );
};
export { MainLayout };
