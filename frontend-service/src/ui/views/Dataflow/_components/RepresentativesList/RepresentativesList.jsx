import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import uniq from 'lodash/uniq';

import uuid from 'uuid';
import styles from './RepresentativesList.module.scss';

import { reducer } from './_functions/Reducers/representativeReducer.js';
import {
  autofocusOnEmptyInput,
  createUnusedOptionsList,
  getAllDataProviders,
  getInitialData,
  onAddProvider,
  onDataProviderIdChange,
  onDeleteConfirm,
  onExportLeadReportersTemplate,
  onKeyDown,
  isValidEmail
} from './_functions/Utils/representativeUtils';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const RepresentativesList = ({
  dataflowRepresentatives,
  dataflowId,
  isActiveManageRolesDialog,
  setDataProviderSelected,
  setFormHasRepresentatives,
  setHasRepresentativesWithoutDatasets
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    allPossibleDataProvidersNoSelect: [],
    dataflowRepresentatives: dataflowRepresentatives,
    dataProvidersTypesList: [],
    initialRepresentatives: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    representativesHaveError: [],
    representativeIdToDelete: '',
    representatives: [],
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: [],
    isLoading: false
  };
  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  useEffect(() => {
    if (isActiveManageRolesDialog === false && !isEmpty(formState.representativesHaveError)) {
      formDispatcher({
        type: 'REFRESH'
      });
    }
  }, [isActiveManageRolesDialog]);

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formState.representatives, formDispatcher);
    }
    setDataProviderSelected(formState.selectedDataProviderGroup);
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

  const providerAccountInputColumnTemplate = representative => {
    let inputData = representative.providerAccount;

    let hasError = formState.representativesHaveError.includes(representative.representativeId);

    const labelId = uuid.v4();

    const onAccountChange = (account, dataProviderId) => {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.dataProviderId === dataProviderId
      );

      thisRepresentative.providerAccount = account;

      let representativesHaveError;

      if (isValidEmail(account)) {
        representativesHaveError = formState.representativesHaveError.filter(
          representativeId => representativeId !== thisRepresentative.representativeId
        );
      } else {
        representativesHaveError = formState.representativesHaveError;
        representativesHaveError.unshift(thisRepresentative.representativeId);
      }

      formDispatcher({
        type: 'ON_ACCOUNT_CHANGE',
        payload: {
          representatives,
          representativesHaveError: uniq(representativesHaveError)
        }
      });
    };

    return (
      <>
        <div className={`formField ${hasError ? 'error' : undefined}`} style={{ marginBottom: '0rem' }}>
          <input
            autoFocus={isNil(representative.representativeId)}
            className={representative.hasDatasets ? styles.disabled : undefined}
            disabled={representative.hasDatasets}
            id={isEmpty(inputData) ? 'emptyInput' : labelId}
            onBlur={() => {
              representative.providerAccount = representative.providerAccount.toLowerCase();
              isValidEmail(representative.providerAccount) &&
                onAddProvider(formDispatcher, formState, representative, dataflowId);
            }}
            onChange={event => onAccountChange(event.target.value, representative.dataProviderId)}
            onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
            placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
            value={inputData}
          />
          <label htmlFor={isEmpty(inputData) ? 'emptyInput' : labelId} className="srOnly">
            {resources.messages['manageRolesDialogInputPlaceholder']}
          </label>
        </div>
      </>
    );
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
          <Button
            className={`${styles.exportTemplate} p-button-secondary ${
              !isEmpty(formState.selectedDataProviderGroup) ? 'p-button-animated-blink' : ''
            }`}
            disabled={isEmpty(formState.selectedDataProviderGroup)}
            icon={'export'}
            label={resources.messages['exportLeadReportersTemplate']}
            onClick={() => {
              try {
                onExportLeadReportersTemplate(formState.selectedDataProviderGroup);
              } catch (error) {
                console.error(error);
                notificationContext.add({
                  type: 'EXPORT_DATAFLOW_LEAD_REPORTERS_TEMPLATE_FAILED_EVENT'
                });
              }
            }}
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
              body={providerAccountInputColumnTemplate}
              header={resources.messages['manageRolesDialogAccountColumn']}
            />
            <Column body={dropdownColumnTemplate} header={resources.messages['manageRolesDialogDataProviderColumn']} />
            <Column
              body={deleteBtnColumnTemplate}
              className={styles.emptyTableHeader}
              header={resources.messages['deleteRepresentativeButtonTableHeader']}
              style={{ width: '60px' }}
            />
          </DataTable>
        </div>
      ) : (
        <p className={styles.chooseRepresentative}>{resources.messages['manageRolesDialogNoRepresentativesMessage']}</p>
      )}

      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          onConfirm={() => {
            onDeleteConfirm(formDispatcher, formState);
          }}
          onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
          visible={formState.isVisibleConfirmDeleteDialog}
          header={resources.messages['manageRolesDialogConfirmDeleteHeader']}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {resources.messages['manageRolesDialogConfirmDeleteQuestion']}
        </ConfirmDialog>
      )}
    </div>
  );
};

export { RepresentativesList };
