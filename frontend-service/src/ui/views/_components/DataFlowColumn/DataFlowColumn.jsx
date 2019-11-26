import React, { useEffect, useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { isUndefined } from 'lodash';

import styles from './DataFlowColumn.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CreateDataflowForm } from './_components/CreateDataflowForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

const DataflowColumn = withRouter(
  ({
    navTitle,
    dataflowTitle,
    components = [],
    entity,
    createDataflowButtonTitle,
    subscribeButtonTitle,
    history,
    style,
    match
  }) => {
    const resources = useContext(ResourcesContext);
    const [createDataflowDialogVisible, setCreateDataflowDialogVisible] = useState(false);
    const [isCustodian, setIsCustodian] = useState(false);
    const [isFormReset, setIsFormReset] = useState(true);
    const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);
    const user = useContext(UserContext);

    useEffect(() => {
      if (!isUndefined(user.accessRole)) {
        setIsCustodian(UserService.hasPermission(user, [config.permissions.CUSTODIAN]));
      }
    }, [user]);

    const setVisibleHandler = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onConfirmSubscribeHandler = () => {
      setSubscribeDialogVisible(false);
    };

    const onCreateDataflow = () => {
      setCreateDataflowDialogVisible(false);
    };

    const onHideDialog = () => {
      setCreateDataflowDialogVisible(false);
      setIsFormReset(false);
    };

    return (
      <div className="nav rep-col-12 rep-col-sm-2">
        <h2 className={styles.title}>{navTitle}</h2>
        {components.includes('search') && (
          <div className="navSection">
            <input
              className={styles.searchInput}
              id=""
              placeholder={resources.messages['searchDataflow']}
              type="text"
              disabled
            />
          </div>
        )}
        <div className="navSection">
          {isCustodian && components.includes('createDataflow') && (
            <Button
              className={`${styles.columnButton} p-button-primary`}
              icon="plus"
              label={createDataflowButtonTitle}
              onClick={() => {
                setCreateDataflowDialogVisible(true);
                setIsFormReset(true);
              }}
              style={{ textAlign: 'left' }}
            />
          )}

          <Button
            className={styles.columnButton}
            icon="plus"
            label={subscribeButtonTitle}
            onClick={() => {
              setVisibleHandler(setSubscribeDialogVisible, true);
            }}
            style={style}
            disabled
          />
        </div>

        <Dialog
          header={resources.messages['createNewDataflow']}
          visible={createDataflowDialogVisible}
          className={styles.dialog}
          dismissableMask={false}
          onHide={onHideDialog}>
          <CreateDataflowForm
            isFormReset={isFormReset}
            onCreate={onCreateDataflow}
            setCreateDataflowDialogVisible={setCreateDataflowDialogVisible}></CreateDataflowForm>
        </Dialog>

        <ConfirmDialog
          header={resources.messages['subscribeButtonTitle']}
          maximizable={false}
          labelCancel={resources.messages['close']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmSubscribeHandler}
          onHide={() => setVisibleHandler(setSubscribeDialogVisible, false)}
          visible={subscribeDialogVisible}>
          {resources.messages['subscribeDataflow']}
        </ConfirmDialog>
      </div>
    );
  }
);

export { DataflowColumn };
