import React from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './TitleWithItem.module.scss';

const TitleWithItem = React.memo(({ icon, iconSize, title, subtitle, items }) => {
  return (
    <div className={styles.rowContainer}>
      <div className={styles.titleWrap}>
        <div className={styles.iconWrap}>
          <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons(icon)} style={{ fontSize: iconSize }} />
        </div>
        <div className={styles.textWrap}>
          <span className={styles.title}>{title}</span>
          <span className={styles.subtitle}>{subtitle}</span>
        </div>
      </div>
      <div className={styles.itemsContainer}>
        {items.map(item => (
          <div className={styles.itemContainer}>{item}</div>
        ))}
      </div>
    </div>
  );
});

export { TitleWithItem };
