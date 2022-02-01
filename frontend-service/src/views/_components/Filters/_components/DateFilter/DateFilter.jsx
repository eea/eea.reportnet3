import { useContext, useEffect, useRef, useState } from 'react';
import { useRecoilState } from 'recoil';

import isDate from 'lodash/isDate';
import isEmpty from 'lodash/isEmpty';
import uniqueId from 'lodash/uniqueId';

//import styles from '../../Filters.module.scss';
import styles from './DateFilter.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { SortButton } from '../SortButton';

import { filterByStore } from '../../_functions/Stores/filterStore';

import { UserContext } from 'views/_functions/Contexts/UserContext';

export const DateFilter = ({ isLoading, option, recoilId }) => {
  const { userProps } = useContext(UserContext);

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  const [isLabelAnimated, setIsLabelAnimated] = useState(false);

  const calendarRefs = useRef([]);

  const inputId = uniqueId();

  useEffect(() => {
    const listener = event => {
      for (const position in isLabelAnimated) {
        const key = Object.keys(isLabelAnimated[position])[0];
        if (!calendarRefs.current[key] || calendarRefs.current[key].contains(event.target)) {
          return;
        }

        setIsLabelAnimated(isEmpty(filterBy[key]));
      }
    };
    document.addEventListener('mousedown', listener);
    document.addEventListener('touchstart', listener);

    return () => {
      document.removeEventListener('mousedown', listener);
      document.removeEventListener('touchstart', listener);
    };
  }, [calendarRefs, isLabelAnimated, filterBy]);

  const parseDateValues = values => {
    if (!values) {
      return [];
    }

    return values.map(value => {
      if (!value) {
        return null;
      }

      return isDate(value) ? value.getTime() : new Date(value);
    });
  };

  return (
    <div className={styles.block} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} />
      <div
        className={`p-float-label ${styles.label} ${styles.dateBlock} } ${
          filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
        }`}
        id={`calendar_${option.key}`}
        ref={el => (calendarRefs.current[option.key] = el)}>
        <Calendar
          baseZIndex={9999}
          dateFormat={userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
          inputClassName={styles.inputFilter}
          inputId={inputId}
          monthNavigator={true}
          onChange={event => setFilterBy({ [option.key]: parseDateValues(event.target.value) })}
          onFocus={() => setIsLabelAnimated(true)}
          readOnlyInput={true}
          selectionMode="range"
          value={parseDateValues(filterBy[option.key])}
          yearNavigator={true}
        />

        <label className={isLabelAnimated ? styles.label : styles.labelDown} htmlFor={inputId}>
          {option.label || ''}
        </label>

        {!isEmpty(filterBy[option.key]) && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => {
              setFilterBy({});
              setIsLabelAnimated(false);
            }}
          />
        )}
      </div>
    </div>
  );
};
