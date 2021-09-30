import { useContext, useState } from 'react';

import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';

import { Button } from 'views/_components/Button';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputMask } from 'views/_components/InputMask';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const TimezoneCalendar = ({ onSaveDate = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);
  dayjs.extend(utc);
  dayjs.extend(timezone);

  const [date, setDate] = useState(new Date());
  console.log(dayjs('2014-06-01 12:00').tz('America/New_York'));
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
    return <Calendar inline={true} onChange={e => setDate(e.value)} showWeek={true} value={date} />;
  };

  const renderInput = () => {
    return <InputText onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };
  const renderInputMask = () => {
    return <InputMask onChange={e => setDate(new Date(e.target.value))} value={date} />;
  };

  const renderTimezoneDropdown = () => {};

  return (
    <div>
      {renderCalendar()}
      {renderInput()}
      {renderTimezoneDropdown()}
      {renderButtons()}
    </div>
  );
};
