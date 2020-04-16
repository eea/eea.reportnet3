import React, { useContext } from 'react';

import styles from './UserData.module.scss';
import { UserImg } from './_components/UserImg';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { routes } from 'ui/routes';
import { getUrl } from 'core/infrastructure/CoreUtils';

const UserData = () => {
  const userContext = useContext(UserContext);
  const resources = useContext(ResourcesContext);

  return (
    <div className={styles.userDataContainer}>
      <div className={styles.userLogoBoxContainer}>
        <UserImg />
      </div>
      <div className={styles.userName}>{userContext.preferredUsername}</div>
      <div className={styles.userMail}>{userContext.preferredUsername}@reportnet.net</div>
      <div>
        <a
          type="button"
          disabled
          style={{ cursor: 'pointer' }}
          href={getUrl(routes.PRIVACY_STATEMENT)}
          target="_blank"
          rel="noopener noreferrer">
          {resources.messages['privacyPolicy']}
        </a>
      </div>
    </div>
  );
};

export { UserData };
