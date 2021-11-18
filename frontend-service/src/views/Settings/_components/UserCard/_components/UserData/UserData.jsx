import { useContext } from 'react';
import isUndefined from 'lodash/isUndefined';

import styles from './UserData.module.scss';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { routes } from 'conf/routes';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { UserImage } from './_components/UserImage';

const UserData = () => {
  const userContext = useContext(UserContext);
  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.userDataContainer}>
      <div className={styles.userLogoBoxContainer}>
        <UserImage />
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
          disabled
          href={getUrl(routes.PRIVACY_POLICY_EIONET)}
          rel="noopener noreferrer"
          style={{ cursor: 'pointer' }}
          target="_blank"
          type="button">
          {resourcesContext.messages['privacyPolicy']}
        </a>
      </div>
    </div>
  );
};

export { UserData };
