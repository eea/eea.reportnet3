import { React, useContext } from 'react';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserService } from 'services/UserService';



import styles from './SessionExpireDialog.module.scss';

export const SessionExpireDialog = ({ visible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const handleLogoutAndRedirect = async () => {
    try {
      await UserService.logout();
    } catch (error) {
      console.error('Header - userLogout - logout.', error);
    }
    window.location.reload();
  };

  return (
    <div className={styles.SessionExpireDialog}>
      <Dialog
        className="session-expired-dialog"
        disabledCancel={true}
        focusOnShow={true}
        footer={
          <Button
            className="p-button-primary p-button-animated-blink"
            icon="check"
            label="Login"
            onClick={handleLogoutAndRedirect}
          />
        }
        header={resourcesContext.messages['sessionExpiredHeader']}
        onHide={() => {}}
        style={{ minWidth: '20vw', maxWidth: '30vw', maxHeight: '80vh' }}
        visible={visible}
      >
        {resourcesContext?.messages?.sessionExpired || 'Your session has expired. Please log in again.'}
      </Dialog>
    </div>
  );
};