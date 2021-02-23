import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import uuid from 'uuid';

import styles from './RepresentativesList.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { Spinner } from 'ui/views/_components/Spinner';

import { RepresentativeService } from 'core/services/Representative';

import { Button } from 'ui/views/_components/Button/Button';
import { InputText } from 'ui/views/_components/InputText/InputText';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { reducer } from './_functions/Reducers/representativeReducer.js';

import {
  autofocusOnEmptyInput,
  createUnusedOptionsList,
  getAllDataProviders,
  getInitialData,
  isValidEmail,
  onAddProvider,
  onDataProviderIdChange,
  onDeleteConfirm,
  onKeyDown
} from './_functions/Utils/representativeUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const RepresentativesList = ({
  dataflowId,
  isActiveManageRolesDialog,
  setFormHasRepresentatives,
  setHasRepresentativesWithoutDatasets
}) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    allPossibleDataProvidersNoSelect: [],
    dataProvidersTypesList: [],
    deleteLeadReporterId: null,
    initialRepresentatives: [],
    isLoading: false,
    isVisibleConfirmDeleteDialog: false,
    isVisibleDialog: { deleteLeadReporter: false, deleteRepresentative: false },
    leadReporters: {},
    leadReportersErrors: {},
    providerWithEmptyInput: null,
    refresher: false,
    representativeIdToDelete: '',
    representatives: [],
    representativesHaveError: [],
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  const { isVisibleDialog } = formState;

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  useEffect(() => {
    console.log('formState.leadReporters', formState.leadReporters);
  }, [formState.leadReporters]);

  useEffect(() => {
    if (isActiveManageRolesDialog === false && !isEmpty(formState.representativesHaveError)) {
      formDispatcher({ type: 'REFRESH' });
    }
  }, [isActiveManageRolesDialog]);

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formState.representatives, formDispatcher);
    }
  }, [formState.selectedDataProviderGroup]);

  useEffect(() => {
    createUnusedOptionsList(formDispatcher);
  }, [formState.allPossibleDataProviders]);

  useEffect(() => {
    autofocusOnEmptyInput(formState);
  }, [formState.representativesHaveError]);

  useEffect(() => {
    if (!isEmpty(formState.representatives)) {
      setFormHasRepresentatives(formState.representatives.length > 1);
    }
  }, [formState.representatives]);

  useEffect(() => {
    if (!isEmpty(formState.representatives) && formState.representatives.length > 1) {
      const representativesNoDatasets = formState.representatives.filter(
        representative => !representative.hasDatasets && !isUndefined(representative.representativeId)
      );

      const representativesHaveDatasets = formState.representatives.filter(
        representative => representative.hasDatasets && !isUndefined(representative.representativeId)
      );

      setHasRepresentativesWithoutDatasets(
        !isEmpty(representativesNoDatasets) && !isEmpty(representativesHaveDatasets)
      );
    }
  }, [formState.representatives]);

  const handleDialogs = (dialog, isVisible) => {
    formDispatcher({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
  };

  const onChangeLeadReporter = async (dataProviderId, leadReporterId, inputValue) => {
    formDispatcher({ type: 'ON_CHANGE_LEAD_REPORTER', payload: { dataProviderId, leadReporterId, inputValue } });
  };

  const onCleanErrors = (dataProviderId, leadReporterId) => {
    formDispatcher({ type: 'CLEAN_UP_ERRORS', payload: { dataProviderId, hasErrors: false, leadReporterId } });
  };

  const onDeleteLeadReporter = async () => {
    try {
      const response = await RepresentativeService.deleteLeadReporter(formState.deleteLeadReporterId);

      if (response.status >= 200 && response.status <= 299) {
        handleDialogs('deleteLeadReporter', false);
        formDispatcher({ type: 'REFRESH' });
      }
    } catch (error) {
      console.log('error', error);
    }
  };

  const onSubmitLeadReporter = async (inputValue, representativeId, dataProviderId, leadReporterId) => {
    const { addLeadReporter, updateLeadReporter } = RepresentativeService;

    if (isValidEmail(inputValue)) {
      try {
        const response = TextUtils.areEquals(leadReporterId, 'empty')
          ? await addLeadReporter(inputValue, representativeId)
          : await updateLeadReporter(inputValue, leadReporterId, representativeId);

        if (response.status >= 200 && response.status <= 299) {
          formDispatcher({ type: 'REFRESH' });
        }
      } catch (error) {
        console.log('error', error);
        formDispatcher({ type: 'CREATE_ERROR', payload: { dataProviderId, hasErrors: true, leadReporterId } });
      }
    }
    formDispatcher({ type: 'CREATE_ERROR', payload: { dataProviderId, hasErrors: true, leadReporterId } });
  };

  const renderLeadReporterTemplate = representative => {
    const { dataProviderId, representativeId } = representative;

    if (isNil(representative.leadReporters)) return [];

    return representative.leadReporters.map(leadReporter => {
      const reporters = formState.leadReporters[dataProviderId];
      const errors = formState.leadReportersErrors[dataProviderId];

      if (leadReporter.id === 'empty') {
        return (
          <div className={styles.inputWrapper} key={`${leadReporter.id}-${representativeId}`}>
            <InputText
              id={`${leadReporter.id}-${representativeId}`}
              className={errors?.[leadReporter.id] ? styles.hasErrors : undefined}
              onFocus={() => onCleanErrors(dataProviderId, leadReporter.id)}
              placeholder={`New lead reporter`}
              onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
              onBlur={event =>
                onSubmitLeadReporter(event.target.value, representativeId, dataProviderId, leadReporter.id)
              }
              value={reporters[leadReporter.id]}
            />
          </div>
        );
      }

      return (
        <div className={styles.inputWrapper} key={`${leadReporter.id}-${representativeId}`}>
          <InputText
            id={`${leadReporter.id}-${representativeId}`}
            className={errors?.[leadReporter.id] ? styles.hasErrors : undefined}
            onFocus={() => onCleanErrors(dataProviderId, leadReporter.id)}
            onBlur={event =>
              onSubmitLeadReporter(event.target.value, representativeId, dataProviderId, leadReporter.id)
            }
            onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
            value={reporters[leadReporter.id]?.account}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton}`}
            icon={'trash'}
            onClick={() => {
              handleDialogs('deleteLeadReporter', true);
              formDispatcher({ type: 'LEAD_REPORTER_DELETE_ID', payload: { id: leadReporter.id } });
            }}
          />
        </div>
      );
    });
  };

  const dropdownColumnTemplate = representative => {
    const selectedOptionForThisSelect = formState.allPossibleDataProviders.filter(
      option => option.dataProviderId === representative.dataProviderId
    );

    const remainingOptionsAndSelectedOption = orderBy(
      selectedOptionForThisSelect.concat(formState.unusedDataProvidersOptions),
      ['label'],
      ['asc']
    );

    const labelId = uuid.v4();

    return (
      <>
        <label htmlFor={labelId} className="srOnly">
          {resources.messages['manageRolesDialogInputPlaceholder']}
        </label>
        <select
          className={
            representative.hasDatasets ? `${styles.disabled} ${styles.selectDataProvider}` : styles.selectDataProvider
          }
          disabled={representative.hasDatasets}
          id={labelId}
          onBlur={() => onAddProvider(formDispatcher, formState, representative, dataflowId)}
          onChange={event => {
            onDataProviderIdChange(formDispatcher, event.target.value, representative, formState);
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
          value={representative.dataProviderId}>
          {remainingOptionsAndSelectedOption.map(provider => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={provider.dataProviderId}>
                {provider.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = representative => {
    return isNil(representative.representativeId) || representative.hasDatasets ? (
      <></>
    ) : (
      <ActionsColumn
        onDeleteClick={() => {
          formDispatcher({
            type: 'SHOW_CONFIRM_DIALOG',
            payload: { representativeId: representative.representativeId }
          });
        }}
      />
    );
  };

  if (isEmpty(formState.representatives)) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.container}>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>{resources.messages['manageRolesDialogHeader']}</div>

        <div>
          <label>{resources.messages['manageRolesDialogDropdownLabel']} </label>
          <Dropdown
            ariaLabel={'dataProviders'}
            disabled={formState.representatives.length > 1}
            name="dataProvidersDropdown"
            onChange={event => formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: event.target.value })}
            optionLabel="label"
            options={formState.dataProvidersTypesList}
            placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
            className={styles.dataProvidersDropdown}
            value={formState.selectedDataProviderGroup}
          />
        </div>
      </div>

      {!isNil(formState.selectedDataProviderGroup) && !isEmpty(formState.allPossibleDataProviders) ? (
        <div className={styles.table}>
          {formState.isLoading && <Spinner className={styles.spinner} style={{ top: 0, left: 0, zIndex: 6000 }} />}
          <DataTable
            value={
              formState.representatives.length > formState.allPossibleDataProvidersNoSelect.length
                ? formState.representatives.filter(representative => !isNil(representative.representativeId))
                : formState.representatives
            }>
            <Column
              body={deleteBtnColumnTemplate}
              className={styles.emptyTableHeader}
              header={resources.messages['deleteRepresentativeButtonTableHeader']}
              style={{ width: '60px' }}
            />
            <Column body={dropdownColumnTemplate} header={resources.messages['manageRolesDialogDataProviderColumn']} />
            <Column body={renderLeadReporterTemplate} header={resources.messages['manageRolesDialogAccountColumn']} />
          </DataTable>
        </div>
      ) : (
        <p className={styles.chooseRepresentative}>{resources.messages['manageRolesDialogNoRepresentativesMessage']}</p>
      )}

      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['manageRolesDialogConfirmDeleteHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteConfirm(formDispatcher, formState)}
          onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
          visible={formState.isVisibleConfirmDeleteDialog}>
          {resources.messages['manageRolesDialogConfirmDeleteQuestion']}
        </ConfirmDialog>
      )}

      {isVisibleDialog.deleteLeadReporter && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={'DELETE LEAD REPORTER'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteLeadReporter()}
          onHide={() => {
            handleDialogs('deleteLeadReporter', false);
            formDispatcher({ type: 'LEAD_REPORTER_DELETE_ID', payload: { id: null } });
          }}
          visible={isVisibleDialog.deleteLeadReporter}>
          DELETE LEAD REPORTER
        </ConfirmDialog>
      )}
    </div>
  );
};

export { RepresentativesList };
