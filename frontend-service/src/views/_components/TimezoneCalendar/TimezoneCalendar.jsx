import { useContext, useEffect, useState, useRef } from 'react';

import customParseFormat from 'dayjs/plugin/customParseFormat';
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';

import styles from './TimezoneCalendar.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputMask } from 'views/_components/InputMask';
import { InputText } from 'views/_components/InputText';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';

const offsetOptions = [
  { value: -12, label: '-12:00' },
  { value: -11, label: '-11:00' },
  { value: -10, label: '-10:00' },
  { value: -9, label: '-09:00' },
  { value: -8, label: '-08:00' },
  { value: -7, label: '-07:00' },
  { value: -6, label: '-06:00' },
  { value: -5, label: '-05:00' },
  { value: -4, label: '-04:00' },
  { value: -3, label: '-03:00' },
  { value: -2, label: '-02:00' },
  { value: -1, label: '-01:00' },
  { value: 0, label: '+00:00' },
  { value: 1, label: '+01:00' },
  { value: 2, label: '+02:00' },
  { value: 3, label: '+03:00' },
  { value: 4, label: '+04:00' },
  { value: 5, label: '+05:00' },
  { value: 6, label: '+06:00' },
  { value: 7, label: '+07:00' },
  { value: 8, label: '+08:00' },
  { value: 9, label: '+09:00' },
  { value: 10, label: '+10:00' },
  { value: 11, label: '+11:00' },
  { value: 12, label: '+12:00' }
];

export const TimezoneCalendar = ({ onSaveDate = () => {}, value, isInModal, isDisabled }) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(customParseFormat);

  const calendarRef = useRef();

  const [date, setDate] = useState('');
  const [inputValue, setInputValue] = useState('00:00:00');
  const [selectedOffset, setSelectedOffset] = useState({ value: 0, label: '+00:00' });
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    setInputValue(dayjs(value).format('HH:mm:ss').toString());
    setDate(value);
  }, []);

  useEffect(() => {
    if (isInModal && dayjs(date).isValid()) {
      onSaveDate(dayjs.utc(date).utcOffset(selectedOffset.value));
    }
  }, [date, selectedOffset.value]);

  const renderButtons = () => {
    if (isInModal) {
      return;
    }
    return (
      <div className={styles.buttonWrapper}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          disabled={!dayjs(new Date(date)).isValid() || hasError}
          icon="save"
          label={resourcesContext.messages['save']}
          onClick={() => onSaveDate(dayjs.utc(date).utcOffset(selectedOffset.value))}
        />
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar
        disabled={isDisabled}
        inline
        monthNavigator
        onChange={e => {
          checkError(dayjs(e.value).format('HH:mm:ss').toString());
          setDate(e.value);
        }}
        ref={calendarRef}
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

  const renderInput = () => {
    return <InputText onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };

  const checkIsCorrectTimeFormat = time => RegularExpressions['time24'].test(time);

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
            const [hour, minute, second] = e.value.split(':');

            setDate(new Date(dayjs(date).hour(hour).minute(minute).second(second).format('ddd/MMMDD/YYYY HH:mm:ss')));
          }
        }}
        value={inputValue}
      />
    );
  };
  const renderDropdown = () => {
    return (
      <Dropdown
        appendTo={document.body}
        className={styles.dropdown}
        disabled={isDisabled}
        onChange={e => setSelectedOffset(e.value)}
        optionLabel="label"
        optionValue="value"
        options={offsetOptions}
        value={selectedOffset}
      />
    );
  };

  return (
    <div className={styles.container}>
      {renderCalendar()}
      {renderInput()}
      <div className={styles.inputMaskWrapper}>
        {renderInputMask()}
        <div className={styles.utc}>
          <span className={styles.label}>{resourcesContext.messages['utc']}</span>
          <TooltipButton
            message={resourcesContext.messages['dateTimeWarningTooltip']}
            uniqueIdentifier={'dateTimeWarningTooltip'}
          />
          {renderDropdown()}
        </div>
      </div>
      {renderButtons()}
    </div>
  );
};
