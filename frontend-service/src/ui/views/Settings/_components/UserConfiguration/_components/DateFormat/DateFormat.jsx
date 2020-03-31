import React, { Component, useState, useContext } from 'react';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const DateFormat = () => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const ChangeDateFormat = e => userContext.dateFormat(e.value);

  return (
    <React.Fragment>
      {' '}
      <h3>{resources.messages['dateFormat']}</h3>
      <h4>{resources.messages['dateFormatWarning']}</h4>
      <Dropdown
        name="rowPerPage"
        placeholder="select"
        options={resources.userParameters['dateFormat']}
        onChange={ChangeDateFormat}
        value={userContext.userProps.dateFormat}
      />
    </React.Fragment>
  );
};
