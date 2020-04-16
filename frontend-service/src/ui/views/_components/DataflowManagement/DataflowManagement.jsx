import React, { Fragment, useContext, useReducer } from 'react';

import styles from './DataflowManagement.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataflowManagementForm } from './_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { ReportingObligations } from './_components/ReportingObligations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { dataflowManagementReducer } from './_functions/Reducers/dataflowManagementReducer';

export const DataflowManagement = ({
  dataflowId,
  isEditForm,
  onCreateDataflow,
  onEditDataflow,
  onManageDialogs,
  state
}) => {
  const resources = useContext(ResourcesContext);

  const dataflowManagementInitialState = {
    name: isEditForm ? state.name : '',
    description: isEditForm ? state.description : '',
    obligation:
      isEditForm && state.obligations
        ? { id: state.obligations.obligationId, title: state.obligations.title }
        : { id: null, title: '' },
    obligationPrevState:
      isEditForm && state.obligations
        ? { id: state.obligations.obligationId, title: state.obligations.title }
        : { id: null, title: '' }
  };

  const [dataflowManagementState, dataflowManagementDispatch] = useReducer(
    dataflowManagementReducer,
    dataflowManagementInitialState
  );

  const isDialogVisible = isEditForm ? 'isEditDialogVisible' : 'isAddDialogVisible';

  const dialogFooter = (
    <Fragment>
      <Button
        icon="check"
        label={resources.messages['ok']}
        onClick={() => {
          onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true);
          getPrevState(dataflowManagementState.obligation);
        }}
      />
      <Button
        icon="cancel"
        className="p-button-secondary"
        label={resources.messages['cancel']}
        onClick={() => onHideObligationDialog()}
      />
    </Fragment>
  );

  const getPrevState = data =>
    dataflowManagementDispatch({ type: 'PREV_STATE', payload: { id: data.id, title: data.title } });

  const onHideDataflowDialog = () => {
    onManageDialogs(isDialogVisible, false);
    onResetData();
  };

  const onHideObligationDialog = () => {
    onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true);
    onResetObl();
  };

  const onLoadData = ({ name, description }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () =>
    dataflowManagementDispatch({ type: 'RESET_STATE', payload: { resetData: dataflowManagementInitialState } });

  const onResetObl = () =>
    dataflowManagementDispatch({ type: 'ON_LOAD_OBLIGATION', payload: dataflowManagementState.obligationPrevState });

  return (
    <Fragment>
      {state.isRepObDialogVisible && (
        <Dialog
          footer={dialogFooter}
          header={resources.messages['reportingObligations']}
          onHide={() => onHideObligationDialog()}
          style={{ width: '95%' }}
          visible={state.isRepObDialogVisible}>
          <ReportingObligations getObligation={onLoadObligation} oblChecked={dataflowManagementState.obligation} />
        </Dialog>
      )}

      {(state.isAddDialogVisible || state.isEditDialogVisible) && (
        <Dialog
          className={styles.dialog}
          header={resources.messages[isEditForm ? 'updateDataflow' : 'createNewDataflow']}
          onHide={() => onHideDataflowDialog()}
          visible={state.isAddDialogVisible || state.isEditDialogVisible}>
          <DataflowManagementForm
            data={dataflowManagementState}
            dataflowId={dataflowId}
            getData={onLoadData}
            isEditForm={isEditForm}
            onCancel={() => onHideDataflowDialog()}
            onCreate={onCreateDataflow}
            onEdit={onEditDataflow}
            onResetData={onResetData}
            onSearch={() => onManageDialogs('isRepObDialogVisible', true, isDialogVisible, false)}
            refresh={isEditForm ? state.isEditDialogVisible : state.isAddDialogVisible}
          />
        </Dialog>
      )}
    </Fragment>
  );
};
