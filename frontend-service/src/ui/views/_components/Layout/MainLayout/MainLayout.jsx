import React, { Fragment, useContext, useEffect } from 'react';

import { isUndefined } from 'lodash';

import styles from './MainLayout.module.css';

import { Navigation } from './_components';
import { Footer } from './_components';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

const MainLayout = ({ children }) => {
  //check userContext
  const user = useContext(UserContext);
  useEffect(() => {
    async function fetchData() {
      if (isUndefined(user.id)) {
        try {
          const userObject = await UserService.refreshToken();
          user.onTokenRefresh(userObject);
        } catch (error) {
          const logout = await UserService.logout();
          user.onLogout();
        }
      }
    }
    fetchData();
  }, []);
  return (
    <Fragment>
      <Navigation />
      <div className={styles.disclaimer}>
        <span className="p-messages-icon pi  pi-info-circle"></span>
        BETA VERSION: This is test Data. Any change would not affect the real data. So feel free trying your stuff.
      </div>
      <div className={styles.mainContent}>{children}</div>
      <Footer />
    </Fragment>
  );
};
export { MainLayout };
