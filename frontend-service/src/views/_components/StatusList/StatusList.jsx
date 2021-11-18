import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { LevelError } from 'views/_components/LevelError';

export const StatusList = ({ filterDispatch, filteredStatusTypes, statusTypes }) => {
  const statusTypesFilters = statusTypes.map((label, i) => {
    label = label.toString();
    const labelLowerCase = label.toLowerCase();

    return (
      <li className={styles.listItem} key={labelLowerCase}>
        <input
          className={styles.checkbox}
          defaultChecked={!filteredStatusTypes.includes(label)}
          id={`${labelLowerCase}_${i}`}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'CHECKBOX_ON' : 'CHECKBOX_OFF',
              payload: { label }
            });
          }}
          style={{ backgroundColor: colors[labelLowerCase] }}
          type="checkbox"
        />

        <label className={styles.labelItem} htmlFor={`${labelLowerCase}_${i}`}>
          <LevelError type={labelLowerCase} />
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{statusTypesFilters}</ul>;
};
