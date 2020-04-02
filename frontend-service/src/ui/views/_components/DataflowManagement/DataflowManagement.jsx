import React, { Fragment, useContext } from 'react';

import styles from './DataflowManagement.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataflowManagementForm } from './_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { ReportingObligations } from './_components/ReportingObligations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const DataflowManagement = ({ isEditForm, onCreateDataflow, onEditDataflow, onManageDialogs, state }) => {
  const resources = useContext(ResourcesContext);

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
        onClick={() => onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true)}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        footer={dialogFooter}
        header={resources.messages['reportingObligations']}
        onHide={() => onManageDialogs('isRepObDialogVisible', false, isDialogVisible, true)}
        style={{ width: '80%' }}
        visible={state.isRepObDialogVisible}>
        <ReportingObligations />
      </Dialog>

      <Dialog
        className={styles.dialog}
        header={resources.messages[isEditForm ? 'updateDataflow' : 'createNewDataflow']}
        onHide={() => onManageDialogs(isDialogVisible, false)}
        visible={state.isAddDialogVisible || state.isEditDialogVisible}>
        <DataflowManagementForm
          dataflowData={state}
          isEditForm={isEditForm}
          onCancel={() => onManageDialogs(isDialogVisible, false)}
          onCreate={onCreateDataflow}
          onEdit={onEditDataflow}
          onSearch={() => onManageDialogs('isRepObDialogVisible', true, isDialogVisible, false)}
          refresh={isEditForm ? state.isEditDialogVisible : state.isAddDialogVisible}
        />
      </Dialog>
    </Fragment>
  );
};
