import React from 'react';

import styles from './CountriesListItem.module.css';

function CountriesListItem({ filterDispatch, item }) {
  return (
    <li className={styles.listItem}>
      <input
        id={item}
        className={styles.checkbox}
        type="checkbox"
        defaultChecked={true}
        onChange={e => {
          if (e.target.checked) {
            filterDispatch({
              type: 'COUNTRY_CHECKBOX_ON',
              payload: { label: item }
            });
          } else {
            filterDispatch({
              type: 'COUNTRY_CHECKBOX_OFF',
              payload: { label: item }
            });
          }
        }}
      />
      <label htmlFor={item} className={styles.labelItem}>
        {item}
      </label>
    </li>
  );
}

export { CountriesListItem };
