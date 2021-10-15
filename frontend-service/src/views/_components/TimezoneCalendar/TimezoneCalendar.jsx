import { Fragment, useContext, useEffect, useLayoutEffect, useRef, useState } from 'react';

import customParseFormat from 'dayjs/plugin/customParseFormat';
import dayjs from 'dayjs';
import uniqueId from 'lodash/uniqueId';
import utc from 'dayjs/plugin/utc';

import styles from './TimezoneCalendar.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputMask } from 'views/_components/InputMask';
import { Portal } from 'views/_components/Portal';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';

const offsetOptions = [
  { value: -12, label: '-12:00' },
  { value: -11, label: '-11:00' },
  { value: -10, label: '-10:00' },
  { value: -9.5, label: '-09:30' },
  { value: -9, label: '-09:00' },
  { value: -8, label: '-08:00' },
  { value: -7, label: '-07:00' },
  { value: -6, label: '-06:00' },
  { value: -5, label: '-05:00' },
  { value: -4, label: '-04:00' },
  { value: -3.5, label: '-03:30' },
  { value: -3, label: '-03:00' },
  { value: -2, label: '-02:00' },
  { value: -1, label: '-01:00' },
  { value: 0, label: '+00:00' },
  { value: 1, label: '+01:00' },
  { value: 2, label: '+02:00' },
  { value: 3, label: '+03:00' },
  { value: 3.5, label: '+03:30' },
  { value: 4, label: '+04:00' },
  { value: 4.5, label: '+04:30' },
  { value: 5, label: '+05:00' },
  { value: 5.5, label: '+05:30' },
  { value: 5.75, label: '+05:45' },
  { value: 6, label: '+06:00' },
  { value: 6.5, label: '+06:30' },
  { value: 7, label: '+07:00' },
  { value: 8, label: '+08:00' },
  { value: 8.75, label: '+08:45' },
  { value: 9, label: '+09:00' },
  { value: 9.5, label: '+09:30' },
  { value: 10, label: '+10:00' },
  { value: 10.5, label: '+10:30' },
  { value: 11, label: '+11:00' },
  { value: 12, label: '+12:00' },
  { value: 12.75, label: '+12:45' },
  { value: 13, label: '+13:00' },
  { value: 14, label: '+14:00' }
];

export const TimezoneCalendar = ({
  isDisabled,
  isInModal,
  onClickOutside = () => {},
  onSaveDate = () => {},
  value
}) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(customParseFormat);

  const [date, setDate] = useState('');
  const [inputValue, setInputValue] = useState('');
  const [selectedOffset, setSelectedOffset] = useState({ value: 0, label: '+00:00' });
  const [hasError, setHasError] = useState(false);
  const [position, setPosition] = useState();

  const refPosition = useRef(null);
  const calendarRef = useRef(null);

  const handleClickOutside = event => {
    if (calendarRef.current && !calendarRef.current.contains(event.target)) {
      onClickOutside();
    }
  };

  useEffect(() => {
    document.addEventListener('click', handleClickOutside, true);
    return () => {
      document.removeEventListener('click', handleClickOutside, true);
    };
  });

  useLayoutEffect(() => {
    if (RegularExpressions['UTC_ISO8601'].test(value)) {
      setInputValue(dayjs(value).utc().format('HH:mm:ss').toString());
    } else {
      setInputValue(dayjs(new Date()).utc().format('HH:mm:ss').toString());
    }

    const [day] = value.split('T');
    setDate(new Date(day));
    calculatePosition();
  }, []);

  useEffect(() => {
    if (isInModal && dayjs(date).isValid() && checkIsCorrectTimeFormat(inputValue)) {
      onSaveDate(dayjs.utc(parseDate(date)).utcOffset(selectedOffset.value));
    }
  }, [date, selectedOffset.value, inputValue]);

  const parseDate = dateToParse => {
    const newDate = new Date(dateToParse);
    const [hour, minute, second] = inputValue.split(':');
    const utcDate = Date.UTC(newDate.getFullYear(), newDate.getMonth(), newDate.getDate(), hour, minute, second);
    return utcDate;
  };

  const calculatePosition = () => {
    const positionRect = refPosition?.current?.getBoundingClientRect();
    const bodyRect = document.body.getBoundingClientRect();
    const topOffset = positionRect.top - bodyRect.top - 200;

    setPosition({ left: positionRect.left, top: topOffset });
  };

  const renderButtons = () => {
    if (isInModal) {
      return;
    }
    return (
      <div className={styles.buttonWrapper}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          disabled={!dayjs(parseDate(date)).isValid() || hasError || !checkIsCorrectTimeFormat(inputValue)}
          icon="save"
          label={resourcesContext.messages['save']}
          onClick={() => onSaveDate(dayjs.utc(parseDate(date)).utcOffset(selectedOffset.value))}
        />
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar
        dateFormat="yyyy-mm-dd"
        disabled={isDisabled}
        inline
        monthNavigator
        onChange={e => {
          checkError(inputValue);
          setDate(e.value);
        }}
        value={date}
        yearNavigator
        yearRange="1900:2100"
      />
    );
  };

  const checkError = time => {
    if (checkIsCorrectTimeFormat(time)) {
      setHasError(false);
    } else {
      setHasError(true);
    }
  };

  const checkIsCorrectTimeFormat = time => RegularExpressions['time24'].test(time);

  const renderLabel = () => {
    return (
      <Fragment>
        <span className={styles.labelText}>{resourcesContext.messages['outcome']}:</span>
        <span className={styles.labelDate}>
          {dayjs(date).isValid() && checkIsCorrectTimeFormat(inputValue)
            ? dayjs.utc(parseDate(date)).utcOffset(selectedOffset.value).format('YYYY-MM-DD HH:mm[Z]').toString()
            : '-'}
        </span>
      </Fragment>
    );
  };

  const renderInputMask = () => {
    return (
      <InputMask
        autoClear
        className={`${styles.timeInput} ${hasError ? styles.error : ''}`}
        disabled={isDisabled}
        mask={`99:99:99`}
        onChange={e => {
          checkError(e.value);
          setInputValue(e.target.value);
        }}
        onComplete={e => {
          if (checkIsCorrectTimeFormat(e.value)) {
            setInputValue(e.value);
          }
        }}
        value={inputValue}
      />
    );
  };
  const renderDropdown = () => {
    return (
      <Dropdown
        className={styles.dropdown}
        disabled={isDisabled}
        filter
        filterBy="label"
        onChange={e => setSelectedOffset(e.value)}
        optionLabel="label"
        optionValue="value"
        options={offsetOptions}
        value={selectedOffset}
      />
    );
  };

  return (
    <Fragment>
      <div className={styles.hiddenDiv} ref={refPosition} />
      <Portal>
        <div
          className={`${styles.container} p-datepicker.p-component.p-input-overlay.p-shadow`}
          ref={calendarRef}
          style={{ left: `${position?.left}px`, top: `${position?.top + 40}px` }}>
          {renderCalendar()}
          <div className={styles.footer}>
            <div className={styles.inputMaskWrapper}>
              {renderInputMask()}
              <div className={styles.utc}>
                <span className={styles.label}>{resourcesContext.messages['utc']}</span>
                <TooltipButton
                  message={resourcesContext.messages['dateTimeWarningTooltip']}
                  uniqueIdentifier={uniqueId('dateTimeWarningTooltip_')}
                  usedInPortal={true}
                />
                {renderDropdown()}
              </div>
            </div>
            {renderLabel()}
            {renderButtons()}
          </div>
        </div>
      </Portal>
    </Fragment>
  );
};
