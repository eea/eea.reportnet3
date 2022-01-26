import { Fragment, useEffect, useState } from 'react';

import styles from './ReporterListItem.module.scss';

export const ReporterListItem = ({ datasetSchemaId, filterDispatch, reporter, reporterFilters }) => {
  const [isChecked, setIsChecked] = useState(true);

  useEffect(() => {
    setIsChecked(!reporterFilters.includes(reporter));
  }, [reporterFilters]);

  return (
    <Fragment>
      <input
        checked={isChecked}
        className={styles.checkbox}
        id={`${reporter}_${datasetSchemaId}`}
        onChange={e => {
          setIsChecked(e.target.checked);
          filterDispatch({
            type: e.target.checked ? 'REPORTER_CHECKBOX_ON' : 'REPORTER_CHECKBOX_OFF',
            payload: { label: reporter }
          });
        }}
        type="checkbox"
      />
      <label className={styles.labelItem} htmlFor={`${reporter}_${datasetSchemaId}`}>
        {reporter}
      </label>
    </Fragment>
  );
};
