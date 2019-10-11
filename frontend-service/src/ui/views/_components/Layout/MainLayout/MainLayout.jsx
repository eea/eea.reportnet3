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
    const bodySelector = document.querySelector('body');
    bodySelector.style.overflow = 'hidden auto';
  }, []);
  return (
    <Fragment>
      <Navigation />
      <div className={styles.disclaimer}>
        <span className="p-messages-icon pi  pi-info-circle"></span>
        BETA VERSION FOR TESTING ONLY
        <br />
        The data and illustrated errors are for testing purposes only and do not reflect the actual data delivered by
        any countries featured
      </div>
      <div className={styles.mainContent}>{children}</div>
      <Footer />
    </Fragment>
  );
};
export { MainLayout };
