import { useContext, useState, useEffect } from 'react';

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

export const TimezoneCalendar = ({ onSaveDate = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(timezone);

  const [date, setDate] = useState('');
  const [inputValue, setInputValue] = useState('');
  const [selectedTimeZone, setSelectedTimeZone] = useState({ utcOffset: '+00:00', tzCode: 'Africa/Abidjan' });

  const getUtcOffsets = () => {
    let res = timezones.map(zone => ({ utcOffset: zone.utc, tzCode: zone.tzCode }));
    return uniqBy(res, 'utcOffset');
  };

  useEffect(() => {
    //todo timezone setter
  }, [selectedTimeZone]);

  useEffect(() => {
    console.log(`use effect date  >>>  `, date);
  }, [date]);

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
          onClick={() => setDate('')}
        />
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar
        baseZIndex={9999}
        dateFormat="yy/mm/dd"
        hourFormat="24"
        inline
        monthNavigator
        onChange={e => {
          setDate(e.value);
        }}
        // showTime={true}
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

  const renderInputMask = () => {
    console.log('inputValue', inputValue, `dayjs(new Date(inputValue))`, dayjs(new Date(inputValue)).format());
    return (
      <InputMask
        autoClear
        mask={`9999/99/99 99:99`}
        onChange={e => setInputValue(e.target.value)}
        onComplete={e => {
          setDate(dayjs(inputValue).format());
          setInputValue('');
        }}
        // slotChar="yy/mm/dd hh:mm"
        value={dayjs(date).format()}
        // value={inputValue} // 19650917112
      />
    );
  };
  const renderDropdown = () => {
    return (
      <Dropdown
        onChange={e => setSelectedTimeZone(e.value)}
        optionLabel="utcOffset"
        optionValue="tzCode"
        options={getUtcOffsets()}
        placeholder="Select GMT offset"
        value={selectedTimeZone}
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
      {/* {renderInput()} */}
      {renderButtons()}
    </div>
  );
};
