import { useContext, useState } from 'react';

import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';

import { Button } from 'views/_components/Button';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'views/_components/Dropdown';
// import { InputMask } from 'views/_components/InputMask';
import { InputMask } from 'primereact/inputmask';
import { InputText } from 'views/_components/InputText';

import timezones from 'timezones-list';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { uniqBy } from 'lodash';

export const TimezoneCalendar = ({ onSaveDate = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(timezone);

  const [date, setDate] = useState(new Date());
  const [inputValue, setInputValue] = useState('');
  const [selectedTimeZone, setSelectedTimeZone] = useState({ utcOffset: '+00:00', tzCode: 'Africa/Abidjan' });

  const getUtcOffsets = () => {
    let res = timezones.map(zone => ({ utcOffset: zone.utc, tzCode: zone.tzCode }));
    return uniqBy(res, 'utcOffset');
  };

  const renderButtons = () => {
    return (
      <div>
        <Button
          className="p-button-primary"
          disabled={!dayjs(new Date(date)).isValid()}
          label={resourcesContext.messages['save']}
          onClick={() => onSaveDate(date)}
        />
        <Button className="p-button-secondary" label={resourcesContext.messages['clear']} onClick={() => setDate('')} />
      </div>
    );
  };

  const renderCalendar = () => {
    return (
      <Calendar inline monthNavigator onChange={e => setDate(e.value)} showWeek value={date} yearRange="1900:2100" />
    );
  };

  const renderInput = () => {
    return <InputText onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };
  const renderInputMask = () => {
    return (
      <InputMask
        onBlur={e => setDate(new Date(inputValue))}
        onChange={e => setInputValue(e.target.value)}
        mask={`99-9999-99 99:99:99`}
        value={date}
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

  const renderTimezoneDropdown = () => {};

  return (
    <div>
      {renderCalendar()}
      {renderInputMask()}
      {renderDropdown()}
      {renderInput()}
      {renderTimezoneDropdown()}
      {renderButtons()}
    </div>
  );
};
