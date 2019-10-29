import React from 'react';

import styles from './Title.module.css';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const Title = React.memo(({ title, icon }) => {
  return (
    <div className={styles.Title}>
      <h2>
        <FontAwesomeIcon icon={AwesomeIcons(icon)} style={{ marginRight: '0.3rem' }} />
        {title}
      </h2>
    </div>
  );
});

export { Title };
