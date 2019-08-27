import React, { Fragment } from 'react';

import styles from './MainLayout.module.css';

import { Navigation } from './_components';
import { Footer } from './_components';

const MainLayout = ({ children }) => (
  <Fragment>
    <Navigation />
    <div className={styles.mainContent}>{children}</div>
    <Footer />
  </Fragment>
);
export { MainLayout };
