import React, { Fragment, useContext, useEffect } from 'react';

import styles from './PublicLayout.module.scss';

import { EuFooter } from 'ui/views/_components/Layout/MainLayout/_components/EuFooter';
import { Footer } from 'ui/views/_components/Layout/MainLayout/_components/Footer';
import { Header } from 'ui/views/_components/Layout/MainLayout/_components/Header';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

export const PublicLayout = ({ children }) => {
  const themeContext = useContext(ThemeContext);

  useEffect(() => {
    themeContext.onToggleTheme('light');
  }, []);
  return (
    <Fragment>
      <div className={styles.mainContainer}>
        <Header isPublic={true} />
        <div className={`rep-container`}>
          <div className={`${styles.pageContent} rep-row`}>{children}</div>
        </div>
        <Footer />
        <EuFooter />
      </div>
    </Fragment>
  );
};
