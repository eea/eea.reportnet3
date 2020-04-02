import React, { Fragment, useContext, useReducer } from 'react';

import styles from './DataflowManagement.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataflowManagementForm } from './_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { ReportingObligations } from './_components/ReportingObligations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const DataflowManagement = ({
  dataflowId,
  isEditForm,
  onCreateDataflow,
  onEditDataflow,
  onManageDialogs,
  state
}) => {
  const resources = useContext(ResourcesContext);

  const formReducer = (state, { type, payload }) => {
    switch (type) {
      case 'INITIAL_LOAD':
        return { ...state, ...payload };

      case 'ON_LOAD_DATA':
        return { ...state, name: payload.name, description: payload.description };

      case 'ON_LOAD_OBLIGATION':
        return { ...state, obligation: { id: payload.id, title: payload.title } };

      case 'RESET_STATE':
        return (state = payload.initialData);

      default:
        return state;
    }
  };

  const formInitialState = {
    name: isEditForm ? state.name : '',
    description: isEditForm ? state.description : '',
    obligation: isEditForm ? state.obligation : { id: null, title: '' }
  };

  const [formState, formDispatch] = useReducer(formReducer, formInitialState);

  const isDialogVisible = isEditForm ? 'isEditDialogVisible' : 'isAddDialogVisible';

  const dialogFooter = (
    <Fragment>
      <Button
        icon="check"
        label={resources.messages['ok']}
        onClick={() => onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true)}
      />
      <Button
        icon="cancel"
        className="p-button-secondary"
        label={resources.messages['cancel']}
        onClick={() => {
          onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true);
          onResetObl();
        }}
      />
    </Fragment>
  );

  const onLoadData = ({ name, description }) => formDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) => formDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () => formDispatch({ type: 'RESET_STATE', payload: { initialData: formInitialState } });

  const onResetObl = () => formDispatch({ type: 'ON_LOAD_OBLIGATION', payload: formInitialState.obligation });

  return (
    <Fragment>
      <Dialog
        footer={dialogFooter}
        header={resources.messages['reportingObligations']}
        onHide={() => onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true)}
        style={{ width: '80%' }}
        visible={state.isRepObDialogVisible}>
        <ReportingObligations oblChecked={formState.obligation} getObligation={onLoadObligation} />
      </Dialog>

      <Dialog
        className={styles.dialog}
        header={resources.messages[isEditForm ? 'updateDataflow' : 'createNewDataflow']}
        onHide={() => onManageDialogs(isDialogVisible, false)}
        visible={state.isAddDialogVisible || state.isEditDialogVisible}>
        <DataflowManagementForm
          data={formState}
          dataflowId={dataflowId}
          getData={onLoadData}
          isEditForm={isEditForm}
          onCancel={() => onManageDialogs(isDialogVisible, false)}
          onCreate={onCreateDataflow}
          onEdit={onEditDataflow}
          onResetData={onResetData}
          onSearch={() => onManageDialogs('isRepObDialogVisible', true, isDialogVisible, false)}
          refresh={isEditForm ? state.isEditDialogVisible : state.isAddDialogVisible}
        />
      </Dialog>
    </Fragment>
  );
};
