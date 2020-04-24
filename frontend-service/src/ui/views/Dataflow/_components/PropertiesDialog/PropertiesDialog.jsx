import React, { Fragment, useContext, useEffect, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { TreeView } from 'ui/views/_components/TreeView';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { DataflowService } from 'core/services/Dataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { PropertiesUtils } from './_functions/Utils/PropertiesUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const PropertiesDialog = ({ dataflowDataState, dataflowId, history, onDeleteDataflow, manageDialogs }) => {
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

  const onConfirmDeleteDataflow = async () => {
    manageDialogs('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true);
    showLoading();
    try {
      const response = await DataflowService.deleteById(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
        notificationContext.add({ type: 'DATAFLOW_DELETE_SUCCESS' });
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

  const parsedDataflowData = { dataflowStatus: dataflowDataState.data.status };
  const parsedObligationsData = PropertiesUtils.parseObligationsData(dataflowDataState, user.userProps.dateFormat);

  const dialogFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        {dataflowDataState.isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DESIGN'] && (
          <Button
            className="p-button-danger p-button-animated-blink"
            label={resources.messages['deleteDataflowButton']}
            icon="trash"
            onClick={() => manageDialogs('isDeleteDialogVisible', true, 'isPropertiesDialogVisible', false)}
          />
        )}
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="cancel"
        label={resources.messages['close']}
        onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        className={styles.propertiesDialog}
        footer={dialogFooter}
        header={resources.messages['properties']}
        onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
        visible={dataflowDataState.isPropertiesDialogVisible}>
        <div className={styles.propertiesWrap}>
          {dataflowDataState.description}
          {parsedObligationsData.map((data, i) => (
            <div key={i} style={{ marginTop: '1rem', marginBottom: '2rem' }}>
              <TreeViewExpandableItem
                items={[{ label: PropertiesUtils.camelCaseToNormal(data.label) }]}
                buttons={
                  data.label === 'obligation'
                    ? [
                        {
                          className: `p-button-secondary-transparent`,
                          icon: 'externalLink',
                          tooltip: resources.messages['viewMore'],
                          onClick: () =>
                            window.open(
                              `http://rod3.devel1dub.eionet.europa.eu/obligations/${dataflowDataState.obligations.obligationId}`
                            )
                        }
                      ]
                    : []
                }>
                <TreeView property={data.data} propertyName={''} />
              </TreeViewExpandableItem>
            </div>
          ))}

          <TreeViewExpandableItem items={[{ label: resources.messages['dataflowDetails'] }]}>
            <TreeView property={parsedDataflowData} propertyName={''} />
          </TreeViewExpandableItem>
        </div>
      </Dialog>

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['delete'].toUpperCase()}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        disabledConfirm={dataflowDataState.deleteInput.toLowerCase() !== dataflowDataState.name.toLowerCase()}
        onConfirm={() => onConfirmDeleteDataflow()}
        onHide={() => ('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true)}
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
            onChange={event => onDeleteDataflow(event)}
            ref={deleteInputRef}
            value={dataflowDataState.deleteInput}
          />
        </p>
      </ConfirmDialog>
    </Fragment>
  );
};
