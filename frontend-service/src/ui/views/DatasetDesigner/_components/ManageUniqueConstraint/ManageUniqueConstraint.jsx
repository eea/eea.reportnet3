import React, { Fragment, useContext } from 'react';

import styles from './ManageUniqueConstraint.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ManageUniqueConstraint = ({ isUpdate, isVisible, manageDialogs }) => {
  const resources = useContext(ResourcesContext);

  const renderDialogLayout = children =>
    isVisible && (
      <Dialog
        className={styles.dialog}
        footer={renderFooter}
        header={isUpdate ? 'update' : 'create'}
        onHide={() =>
          manageDialogs('manageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true)
        }
        style={{ width: '975px' }}
        visible={isVisible}>
        {children}
      </Dialog>
    );

  const renderFooter = (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['create']}
        onClick={() =>
          manageDialogs('manageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true)
        }
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() =>
          manageDialogs('manageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true)
        }
      />
    </Fragment>
  );

  return renderDialogLayout(
    <form>
      <div className={styles.uniqueFormWrap}></div>
    </form>
  );
};
