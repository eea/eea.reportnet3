import React, { Component, useState, useContext } from 'react';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const DefaultRowsPages = () => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const changeRowValue = e => userContext.defaultRowSelected(e.target.value);

  return (
    <React.Fragment>
      {' '}
      <h3>{resources.messages['defaultRowsPage']}</h3>
      <Dropdown
        name="rowPerPage"
        placeholder="select"
        options={[
          { label: 5, value: 5 },
          { label: 10, value: 10 },
          { label: 20, value: 20 },
          { label: 100, value: 100 }
        ]}
        onChange={changeRowValue}
        value={userContext.userProps.defaultRowSelected}
      />
    </React.Fragment>
  );
};
