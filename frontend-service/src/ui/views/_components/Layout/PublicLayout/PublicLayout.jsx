import React, { Fragment, useContext, useEffect, useState } from 'react';

import { config } from 'conf';

import styles from './PublicLayout.module.scss';

import { EuFooter } from 'ui/views/_components/Layout/MainLayout/_components/EuFooter';
import { Footer } from 'ui/views/_components/Layout/MainLayout/_components/Footer';
import { Header } from 'ui/views/_components/Layout/MainLayout/_components/Header';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

export const PublicLayout = ({ children }) => {
  const themeContext = useContext(ThemeContext);

  const mainContentStyle = {
    height: `auto`,
    minHeight: `${window.innerHeight - config.theme.baseHeaderHeight}px`,
    marginTop: 0
  };

  useEffect(() => {
    themeContext.onToggleTheme('light');
  }, []);

  return (
    <Fragment>
      <div className={styles.mainContainer}>
        <Header isPublic={true} />
        <div className={styles.mainContent} style={mainContentStyle}>
          {children}
        </div>
        <Footer />
        <EuFooter />
      </div>
    </Fragment>
  );
};
