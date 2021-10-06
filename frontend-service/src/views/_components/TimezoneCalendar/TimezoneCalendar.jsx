import { useContext, useState, useRef } from 'react';

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

export const TimezoneCalendar = ({ onSaveDate = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(customParseFormat);

  const calendarRef = useRef();

  const [date, setDate] = useState('');
  const [inputValue, setInputValue] = useState('');
  const [selectedOffset, setSelectedOffset] = useState({ value: 0, label: '+00:00' });

  const renderButtons = () => {
    return (
      <div className={styles.buttonRight}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          disabled={!dayjs(new Date(date)).isValid()}
          icon="save"
          label={resourcesContext.messages['save']}
          onClick={() => onSaveDate(dayjs.utc(date).utcOffset(selectedOffset.value))}
        />
        {/* <Button
          className="p-button p-component p-button-secondary p-button-animated-blink p-button-text-icon-left"
          icon="trash"
          label={resourcesContext.messages['clear']}
          onClick={() => {
            setDate('');
            setInputValue('');
          }}
        /> */}
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar
        inline
        monthNavigator
        onChange={e => {
          setDate(e.value);
          setInputValue(dayjs(e.value).format('DD/MM/YYYY HH:mm:ss').toString());
        }}
        // showTime={true}
        ref={calendarRef}
        value={date}
        yearNavigator
        yearRange="1900:2100"
      />
    );
  };

  const renderInput = () => {
    return <InputText onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };

  const renderInputMask = () => {
    return (
      <InputMask
        autoClear
        mask={`99/99/9999 99:99:99`}
        onChange={e => {
          setInputValue(e.target.value);
        }}
        onComplete={e => {
          setDate(new Date(dayjs(e.value, 'DD/MM/YYYY HH:mm:ss').format('ddd/MMMDD/YYYY HH:mm:ss')));
        }}
        value={inputValue}
      />
    );
  };
  const renderDropdown = () => {
    return (
      <Dropdown
        onChange={e => {
          setSelectedOffset(e.value);
        }}
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
      <div className={styles.inputMaskWrapper}>
        {renderInputMask()}
        <span className={styles.label}>{resourcesContext.messages['utc']}</span>
        <TooltipButton
          message={resourcesContext.messages['dateTimeWarningTooltip']}
          tooltipClassName={styles.tooltip}
          uniqueIdentifier={'dateTimeWarningTooltip'}
        />
        {renderDropdown()}
      </div>
      {/* {renderInput()} */}
      {renderButtons()}
    </div>
  );
};
