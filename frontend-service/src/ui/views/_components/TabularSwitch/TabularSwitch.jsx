import React, { useEffect, useState } from 'react';

import styles from './TabularSwitch.module.scss';

const TabularSwitch = ({ elements, className = '', id, onChange, value }) => {
  const [selected, setSelected] = useState(value);

  console.log({ selected, value });
  useEffect(() => {
    onChange(selected);
  }, [selected]);

  return (
    <div className={`${className} ${styles.tabBar}`} id={id}>
      <div className={styles.indicator} style={{ left: selected === value ? 'calc(150px + 1.5rem)' : '1.5rem' }} />
      {elements.map(element => (
        <div
          className={`${styles.tabItem} ${value === element ? styles.selected : null}`}
          onClick={() => {
            setSelected(element);
          }}>
          <p className={styles.tabLabel}>{element}</p>
        </div>
      ))}
      {/* <div
        className={`${styles.tabItem} ${!isWebformView ? styles.selected : null}`}
        onClick={() => {
          onToggleView(false);
          onSelectRecord(null, null);
        }}>
      </div> */}
      {/* <div
        className={`${styles.tabItem} ${isWebformView ? styles.selected : null} ${
          isEmpty(pamsRecords) ? styles.disabled : null
        }`}
        data-for="emptyTableTooltip"
        data-tip
        onClick={() => (isEmpty(pamsRecords) ? {} : onToggleView(true))}>
        <p className={styles.tabLabel}>{resources.messages['details']}</p>
      </div> */}
    </div>
  );
};
export { TabularSwitch };
