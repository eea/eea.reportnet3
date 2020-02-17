import React, {useContext} from 'react';

import {UserContext} from 'ui/views/_functions/Contexts/UserContext';

const UserConfiguration = () => {
  const userContext = useContext(UserContext)
  return <h1 onClick={(e) => {
    userContext.onToggleLogoutConfirm();
  }}>UserConfiguration</h1>;
};

export { UserConfiguration };
