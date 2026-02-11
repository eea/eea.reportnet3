import { Fragment, useContext, useRef, useState } from 'react';
import { Column } from 'primereact/column';
import isEmpty from 'lodash/isEmpty';

import styles from './ManagePreparationSets.module.scss';
import { ManagePreparationSetsService } from 'services/ManagePreparationSetsService';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';
import { DataTable } from 'views/_components/DataTable';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { InputText } from 'views/_components/InputText';
import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { ActionsColumn } from 'views/_components/ActionsColumn';

export const ManagePreparationSets = ({
  dataflowId,
  isDialogVisible,
  onCloseDialog,
  onGetPreparationSetsList,
  preparationSetsList,
  providerId
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [state, setState] = useState({
    actionsButtons: { id: null },
    dialogMode: null,
    isConfirmDeleteButtonDisabled: false,
    isLoading: false,
    isLoadingButton: false,
    loadingStatus: 'idle',
    preparationSetCode: '',
    preparationSetEditing: null,
    preparationSetName: ''
  });

  const inputRef = useRef(null);

  useInputTextFocus(state.dialogMode === 'add', inputRef);

  const changeState = updates => setState(prev => ({ ...prev, ...updates }));

  const isValidPreparationSetName = () => RegularExpressions['preparationSetName'].test(state.preparationSetName);
  const isValidPreparationSetCode = () => RegularExpressions['nonSymbols'].test(state.preparationSetCode);
  const isRepeatedPreparationSetName = () =>
    preparationSetsList?.some(set => TextUtils.areEquals(set.datasetName, state.preparationSetName));
  const isRepeatedPreparationSetCode = () =>
    preparationSetsList?.some(set => TextUtils.areEquals(set.code, state.preparationSetCode));
  const hasEmptyData = () => isEmpty(state.preparationSetName);

  const getPreparationSets = async () => {
    changeState({ loadingStatus: 'pending' });
    try {
      await onGetPreparationSetsList();

      changeState({ loadingStatus: 'success', isLoading: false });
    } catch (error) {
      console.error(error);
      notificationContext.add({ type: 'GET_PREPARATION_SETS_ERROR' }, true);
      changeState({ loadingStatus: 'error', isLoading: false });
    }
  };

  const openDialog = (mode, set = null) => {
    changeState({
      dialogMode: mode,
      preparationSetEditing: set,
      preparationSetName: set?.datasetName || '',
      preparationSetCode: set?.code || ''
    });
  };

  const closeDialog = () => changeState({ dialogMode: null, preparationSetName: '' });

  const confirmDialogAction = async () => {
    changeState({ isLoadingButton: true });
    try {
      if (state.dialogMode === 'add') {
        await ManagePreparationSetsService.addPreparationSet({
          datasetName: state.preparationSetName,
          code: state.preparationSetCode,
          dataflowId,
          providerId
        });
      } else if (state.dialogMode === 'edit') {
        // api call should be added here if needed for update
      } else if (state.dialogMode === 'delete') {
        changeState({ isConfirmDeleteButtonDisabled: true });
        await ManagePreparationSetsService.deletePreparationSet({
          id: state.actionsButtons.id
        });
      }
      closeDialog();
    } catch (error) {
      console.error(error);
      notificationContext.add(
        {
          type: state.dialogMode === 'add' ? 'CREATE_PREPARATION_SET_ERROR' : 'UPDATE_PREPARATION_SET_ERROR',
          content: { customContent: { createError: error?.response?.data?.message } }
        },
        true
      );
    } finally {
      changeState({ isLoadingButton: false, isConfirmDeleteButtonDisabled: false });
      onResetAll();
    }
  };

  const columns = [
    {
      key: 'created',
      header: 'created',
      className: styles.xSmallColumn,
      template: row => (
        <span
          aria-label={row.isCreated ? 'Created' : 'Not created'}
          className={`${styles.createdCheckbox} ${row.isCreated ? styles.checked : ''}`}
          title={row.isCreated ? 'Created' : 'Not created'}
        />
      )
    },

    { key: 'setName', header: 'set name', className: styles.largeColumn, template: p => <p>{p.datasetName}</p> },
    { key: 'code', header: 'code', className: styles.largeColumn, template: p => <p>{p.code}</p> },
    {
      key: 'actions',
      header: 'actions',
      className: styles.smallColumn,
      template: preparationSet => (
        <ActionsColumn
          onDeleteClick={() => {
            changeState({ actionsButtons: { ...state.actionsButtons, id: preparationSet.id } });
            openDialog('delete', preparationSet);
          }}
          // onEditClick={() => {
          //   changeState({ actionsButtons: { ...state.actionsButtons, id: preparationSet.id } });
          //   openDialog('edit', preparationSet);
          // }}
          rowDataId={preparationSet.id}
          rowUpdatingId={state.actionsButtons.id}
        />
      )
    }
  ];

  const onResetAll = () => {
    changeState({
      preparationSetName: ''
    });
    getPreparationSets();
  };

  const validatedPreparationSetName = value => {
    if (!value) return '';
    let validated = value.replace(/[^A-Za-z0-9 _()\-]/g, '');
    validated = validated.replace(/^[^A-Za-z]+/, '');
    return validated;
  };

  const generateCodeFromName = name => {
    if (!name) return '';
    return name
      .toLowerCase()
      .replace(/[ -]+/g, '_')
      .replace(/[^a-z0-9_]/g, '')
      .replace(/^[^a-z]+/, '');
  };

  const renderForm = () => {
    const isEdit = state.dialogMode === 'edit';
    const isNameRepeated = isRepeatedPreparationSetName();
    const isCodeRepeated = isRepeatedPreparationSetCode();
    const shouldHighlightError = isNameRepeated;

    return (
      <div className={styles.addDialog}>
        <div className={styles.inputWrapper}>
          <label className={styles.label}>{resourcesContext.messages['setName']}</label>

          <InputText
            className={shouldHighlightError ? styles.error : ''}
            onChange={e => {
              const rawValue = e.target.value;
              const validatedName = validatedPreparationSetName(rawValue);

              changeState({
                preparationSetName: validatedName,
                ...(isEdit ? {} : { preparationSetCode: generateCodeFromName(validatedName) })
              });
            }}
            placeholder={resourcesContext.messages['setNamePlaceholder']}
            ref={inputRef}
            style={{ margin: '0.3rem 0' }}
            value={state.preparationSetName}
          />
          {isNameRepeated && (
            <small className={styles.errorText}>A preparation set with this name already exists.</small>
          )}
          <small className={styles.inputHint}>
            Must start with a letter. You can use letters, numbers, space, and the symbols underscore (_), dash (-), and
            parentheses ( ).
          </small>
        </div>

        <div className={styles.inputWrapper}>
          <label className={styles.label}>{resourcesContext.messages['code']}</label>

          <InputText
            className={isCodeRepeated ? styles.error : ''}
            disabled
            style={{ margin: '0.3rem 0' }}
            value={state.preparationSetCode}
          />
          {isCodeRepeated && (
            <small className={styles.errorText}>A preparation set with this code already exists.</small>
          )}
        </div>
      </div>
    );
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className={`${styles.buttonLeft} p-button-animated-blink`}
        icon="plus"
        label={resourcesContext.messages['add']}
        onClick={() => openDialog('add')}
      />
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        disabled={state.loadingStatus === 'pending'}
        icon={'refresh'}
        label={resourcesContext.messages['refresh']}
        onClick={() => {
          getPreparationSets();
        }}
      />
    </div>
  );

  return (
    <Fragment>
      <Dialog
        blockScroll={false}
        className="responsiveDialog"
        footer={dialogFooter}
        header={resourcesContext.messages['managePreparationSets']}
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {state.isLoading && (
          <div className={styles.noDataContent}>
            <Spinner className={styles.spinnerPosition} />
          </div>
        )}
        {!state.isLoading && preparationSetsList !== null && (
          <div className={styles.dialogContent}>
            {preparationSetsList?.length === 0 && (
              <div className={styles.noDataContent}>
                <span>{resourcesContext.messages['noData']}</span>
              </div>
            )}
            {preparationSetsList?.length > 0 && (
              <DataTable
                autoLayout
                className={styles.preparationSetsTable}
                hasDefaultCurrentPage
                lazy
                loading={state.loadingStatus === 'pending'}
                reorderableColumns
                resizableColumns
                value={preparationSetsList}>
                {columns.map(col => (
                  <Column
                    body={col.template}
                    className={col.className}
                    columnResizeMode="expand"
                    field={col.key}
                    header={col.header}
                    key={col.key}
                  />
                ))}
              </DataTable>
            )}
          </div>
        )}
      </Dialog>

      {state.dialogMode && state.dialogMode !== 'delete' && (
        <ConfirmDialog
          dialogStyle={{ minWidth: '400px', maxWidth: '600px' }}
          disabledConfirm={
            hasEmptyData() ||
            state.isLoadingButton ||
            isRepeatedPreparationSetName() ||
            isRepeatedPreparationSetCode() ||
            !isValidPreparationSetName() ||
            (!state.dialogMode === 'edit' && !isValidPreparationSetCode())
          }
          header={resourcesContext.messages[state.dialogMode === 'add' ? 'addPreparationSet' : 'edit']}
          iconConfirm={state.isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={confirmDialogAction}
          onHide={closeDialog}
          visible={!!state.dialogMode}>
          {renderForm()}
        </ConfirmDialog>
      )}

      {state.dialogMode === 'delete' && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={state.isConfirmDeleteButtonDisabled}
          header={resourcesContext.messages['deletePreparationSet']}
          iconConfirm={state.isConfirmDeleteButtonDisabled ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={confirmDialogAction}
          onHide={closeDialog}
          visible={state.dialogMode === 'delete'}>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resourcesContext.messages['confirmDeletePreparationSet'], {
                setName: state.preparationSetName
              })
            }}></p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
