import React from 'react';

import styles from './Title.module.css';

import datasetIcon from 'assets/images/dataset_icon.png';

const Title = React.memo(({ title }) => {
  return (
    <div className={styles.Title}>
      <h2>
        <img src={datasetIcon} alt="Dataset" />
        {title}
      </h2>
    </div>
  );
});

export { Title };
