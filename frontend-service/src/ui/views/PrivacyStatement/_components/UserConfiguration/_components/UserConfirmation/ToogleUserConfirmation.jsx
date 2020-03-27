import React, { useContext } from 'react';

import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ToggleUserConfirmation = () => {
  const userContext = useContext(UserContext);
  const resources = useContext(ResourcesContext);

  return (
    <div>
      <h3>{resources.messages['userConfirmationLogout']}</h3>
      <InputSwitch
        checked={userContext.userProps.showLogoutConfirmation}
        style={{ marginRight: '1rem' }}
        onChange={e => userContext.onToggleLogoutConfirm()}
        tooltip={
          userContext.userProps.showLogoutConfirmation === true
            ? resources.messages['toogleConfirmationOff']
            : resources.messages['toogleConfirmationOn']
        }
      />
    </div>
  );
};
