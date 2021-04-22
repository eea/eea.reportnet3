import { useContext } from 'react';
import isUndefined from 'lodash/isUndefined';

import styles from './UserData.module.scss';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { routes } from 'ui/routes';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserImg } from './_components/UserImg';

const UserData = () => {
  const userContext = useContext(UserContext);
  const resources = useContext(ResourcesContext);

  return (
    <div className={styles.userDataContainer}>
      <div className={styles.userLogoBoxContainer}>
        <UserImg />
      </div>
      <div className={styles.userName}>
        {!isUndefined(userContext.firstName) && userContext.firstName !== ''
          ? `${userContext.firstName} ${userContext.lastName}`
          : userContext.preferredUsername}
      </div>
      <div className={styles.userMail}>
        {!isUndefined(userContext.firstName) && userContext.email !== '' && userContext.email}
      </div>
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
