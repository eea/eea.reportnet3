import { memo } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Title.module.scss';

export const Title = memo(({ icon, iconSize, insideTitle, subtitle, title }) => (
  <div className={styles.titleWrap}>
    <div className={styles.iconWrap}>
      <FontAwesomeIcon
        className={styles.icon}
        icon={AwesomeIcons(icon)}
        role="presentation"
        style={{ fontSize: iconSize }}
      />
    </div>
    <div className={styles.textWrap}>
      <h1 className={styles.title}>
        {title}
        <span>{insideTitle}</span>
      </h1>
      <h3 className={styles.subtitle}>{subtitle}</h3>
    </div>
  </div>
));
