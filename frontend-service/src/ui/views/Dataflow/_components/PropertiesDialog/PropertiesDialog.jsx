import React, { Fragment, useContext, useEffect, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { TreeView } from 'ui/views/_components/TreeView';

import { DataflowService } from 'core/services/Dataflow';
import { UserService } from 'core/services/User';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { PropertiesUtils } from './_functions/Utils/PropertiesUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const PropertiesDialog = ({ dataflowDataState, dataflowId, history, onConfirmDelete, onManageDialogs }) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const deleteInputRef = useRef(null);

  useEffect(() => {
    if (dataflowDataState.isDeleteDialogVisible && !isNil(deleteInputRef.current)) {
      deleteInputRef.current.element.focus();
    }
  }, [dataflowDataState.isDeleteDialogVisible]);

  const onDeleteDataflow = async () => {
    onManageDialogs('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true);
    showLoading();
    try {
      const response = await DataflowService.deleteById(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
      } else {
        throw new Error(`Delete dataflow error with this status: ', ${response.status}`);
      }
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DELETE_BY_ID_ERROR',
        content: {
          dataflowId
        }
      });
    } finally {
      hideLoading();
    }
  };

  const parsedData = PropertiesUtils.parseDataToShow(
    config,
    dataflowDataState,
    dataflowId,
    resources.messages,
    user,
    UserService
  );

  const dialogFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        {dataflowDataState.isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DESIGN'] && (
          <Button
            className="p-button-danger p-button-animated-blink"
            label={resources.messages['deleteDataflowButton']}
            icon="trash"
            onClick={() => onManageDialogs('isDeleteDialogVisible', true, 'isPropertiesDialogVisible', false)}
          />
        )}
      </div>
      <Button className="p-button-text-only" label="Generate new API-key" disabled />
      <Button className="p-button-text-only" label="Open Metadata" disabled />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="cancel"
        label={resources.messages['close']}
        onClick={() => onManageDialogs('isPropertiesDialogVisible', false)}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        header={resources.messages['properties']}
        className={styles.propertiesDialog}
        footer={dialogFooter}
        visible={dataflowDataState.isPropertiesDialogVisible}
        onHide={() => onManageDialogs('isPropertiesDialogVisible', false)}>
        <div className={styles.propertiesWrap}>
          {dataflowDataState.description}
          <TreeView property={parsedData} propertyName={''} />
        </div>
      </Dialog>

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['delete'].toUpperCase()}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        disabledConfirm={dataflowDataState.deleteInput.toLowerCase() !== dataflowDataState.name.toLowerCase()}
        onConfirm={() => onDeleteDataflow()}
        onHide={() => onManageDialogs('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true)}
        visible={dataflowDataState.isDeleteDialogVisible}>
        <p>{resources.messages['deleteDataflow']}</p>
        <p
          dangerouslySetInnerHTML={{
            __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
              dataflowName: dataflowDataState.name
            })
          }}></p>
        <p>
          <InputText
            autoFocus={true}
            className={`${styles.inputText}`}
            onChange={event => onConfirmDelete(event)}
            ref={deleteInputRef}
            value={dataflowDataState.deleteInput}
          />
        </p>
      </ConfirmDialog>
    </Fragment>
  );
};
