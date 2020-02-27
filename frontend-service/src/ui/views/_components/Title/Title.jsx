import React from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Title.module.css';

const Title = React.memo(({ icon, iconSize, title, subtitle }) => {
  return (
    <div className={styles.titleWrap}>
      <div className={styles.iconWrap}>
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons(icon)} style={{ fontSize: iconSize }} />
      </div>
      <div className={styles.textWrap}>
        <h1 className={styles.title}>{title}</h1>
        <h3 className={styles.subtitle}>{subtitle}</h3>
      </div>
    </div>
  );
});

export { Title };
