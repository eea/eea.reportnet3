import { useEffect, useState } from 'react';

import styles from './ReporterListItem.module.scss';

const ReporterListItem = ({ datasetSchemaId, filterDispatch, reporter, reporterFilters }) => {
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(!reporterFilters.includes(reporter));
  }, [reporterFilters]);

  return (
    <>
      <input
        id={`${reporter}_${datasetSchemaId}`}
        className={styles.checkbox}
        type="checkbox"
        checked={isChecked}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked ? 'REPORTER_CHECKBOX_ON' : 'REPORTER_CHECKBOX_OFF',
            payload: { label: reporter }
          });
        }}
      />
      <label htmlFor={`${reporter}_${datasetSchemaId}`} className={styles.labelItem}>
        {reporter}
      </label>
    </>
  );
};

export { ReporterListItem };
