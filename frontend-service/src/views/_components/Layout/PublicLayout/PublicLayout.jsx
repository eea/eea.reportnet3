import { useContext, useEffect } from 'react';

import { config } from 'conf';

import styles from './PublicLayout.module.scss';

import { EuFooter } from 'views/_components/Layout/MainLayout/_components/EuFooter';
import { Footer } from 'views/_components/Layout/MainLayout/_components/Footer';
import { Header } from 'views/_components/Layout/MainLayout/_components/Header';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

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
    <div className={styles.mainContainer}>
      <Header isPublic={true} />
      <div className={styles.mainContent} style={mainContentStyle}>
        {children}
      </div>
      <Footer />
      <EuFooter />
    </div>
  );
};
