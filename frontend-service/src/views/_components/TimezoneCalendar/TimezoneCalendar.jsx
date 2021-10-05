import { useContext, useEffect, useRef, useState } from 'react';

import customParseFormat from 'dayjs/plugin/customParseFormat';
import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';

import styles from './TimezoneCalendar.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputMask } from 'views/_components/InputMask';
import { InputText } from 'views/_components/InputText';

import timezones from 'timezones-list';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { uniqBy } from 'lodash';

const offsetOptions = [
  { value: -1, label: '-01:00' },
  { value: 0, label: '+00:00' },
  { value: 1, label: '+01:00' },
  { value: 2, label: '+02:00' }
];

export const TimezoneCalendar = ({ onSaveDate = () => {} }) => {
  dayjs.extend(utc);
  dayjs.extend(timezone);
  dayjs.extend(customParseFormat);

  const resourcesContext = useContext(ResourcesContext);

  const calendarRef = useRef();

  console.log(`calendarRef`, calendarRef);

  const [date, setDate] = useState('');
  const [inputValue, setInputValue] = useState('');
  // const [selectedTimeZone, setSelectedTimeZone] = useState({ utcOffset: '+00:00', tzCode: 'Africa/Abidjan' });
  const [selectedOffset, setSelectedOffset] = useState({ value: 2, label: '+02:00' });

  // const getUtcOffsets = () => {
  //   let res = timezones.map(zone => ({ utcOffset: zone.utc, tzCode: zone.tzCode }));
  //   return uniqBy(res, 'utcOffset');
  // };

  // useEffect(() => {
  //   //todo timezone setter
  // }, [selectedTimeZone]);

  const renderButtons = () => {
    return (
      <div className={styles.buttonRight}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          disabled={!dayjs(new Date(date)).isValid()}
          icon="save"
          label={resourcesContext.messages['save']}
          onClick={() => onSaveDate(date)}
        />
        <Button
          className="p-button p-component p-button-secondary p-button-animated-blink p-button-text-icon-left"
          icon="trash"
          label={resourcesContext.messages['clear']}
          onClick={() => {
            setDate('');
            setInputValue('');
          }}
        />
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar
        dateFormat="DD/MM/YYYY HH:mm:ss"
        inline
        monthNavigator
        onChange={e => {
          setDate(e.value);
          setInputValue(dayjs(e.value).format('DD/MM/YYYY HH:mm:ss').toString());
        }}
        // showTime={true}
        ref={calendarRef}
        showWeek
        value={date}
        yearNavigator
        yearRange="1900:2100"
      />
    );
  };

  const renderInput = () => {
    return <InputText onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };

  const getFullYear = event =>
    new Date(dayjs(event.value, 'DD/MM/YYYY HH:mm:ss').format('ddd/MMMDD/YYYY HH:mm:ssZZ')).getFullYear();

  const renderInputMask = () => {
    return (
      <InputMask
        autoClear
        mask={`99/99/9999 99:99:99`}
        onChange={e => setInputValue(e.target.value)}
        onComplete={event => {
          event.originalEvent.target.value = getFullYear(event);

          setDate(new Date(dayjs(event.value, 'DD/MM/YYYY HH:mm:ss').format('ddd/MMMDD/YYYY HH:mm:ssZZ')));

          calendarRef?.current?.onYearDropdownChange(event.originalEvent);
          // calendarRef?.current?.onMonthDropdownChange(event.originalEvent);
        }}
        value={inputValue}
      />
    );
  };
  const renderDropdown = () => {
    return (
      <Dropdown
        // onChange={e => setSelectedTimeZone(e.value)}
        onChange={e => setSelectedOffset(e.value)}
        optionLabel="label"
        optionValue="value"
        // options={getUtcOffsets()}
        options={offsetOptions}
        placeholder="Select GMT offset"
        // value={selectedTimeZone}
        value={selectedOffset}
      />
    );
  };

  return (
    <div className={styles.container}>
      {renderCalendar()}
      <div className={styles.inputMaskWrapper}>
        {renderInputMask()}
        <span className={styles.label}>GMT</span>
        {renderDropdown()}
      </div>
      {renderInput()}
      {renderButtons()}
    </div>
  );
};
