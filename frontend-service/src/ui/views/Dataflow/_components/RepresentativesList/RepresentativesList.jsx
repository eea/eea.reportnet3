import React, { Fragment, useContext, useEffect, useReducer } from 'react';

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
  onKeyDown,
  isValidEmail
} from './_functions/Utils/representativeUtils';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Button } from 'ui/views/_components/Button/Button';
import { InputText } from 'ui/views/_components/InputText/InputText';

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
    initialRepresentatives: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    representativesHaveError: [],
    representativeIdToDelete: '',
    representatives: [],
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: [],
    isLoading: false,
    providerWithEmptyInput: null
  };
  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

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

  const onAddEmptyLeadReporter = dataProviderId => {
    const updatedRepresentativesList = formState.representatives.map(representative => {
      if (representative.dataProviderId === dataProviderId && representative.leadReporters) {
        // TODO
        representative.leadReporters.unshift({ id: null, account: '' });
      } else if (representative.dataProviderId === dataProviderId && !representative.leadReporters) {
        representative.leadReporters = [{ id: null, account: '' }];
      }
      return representative;
    });
    formDispatcher({
      type: 'ADD_NEW_LEAD_REPORTER',
      payload: { dataProviderId, representatives: updatedRepresentativesList }
    });
  };

  const renderLeadReporterTemplate = representative => {
    const { dataProviderId } = representative;
    // TODO
    const leadReporters = representative.leadReporters || [];
    //------------------------------------------------
    let inputData = representative.leadReporters;

    let hasError = formState.representativesHaveError.includes(representative.representativeId);

    const labelId = uuid.v4();

    const onAccountChange = (account, dataProviderId) => {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.dataProviderId === dataProviderId
      );
      //
      thisRepresentative.leadReporters = account;

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

    //------------------------------------------------

    return leadReporters.map(leadReporter => {
      if (leadReporter.id === null) {
        return (
          <div className={styles.inputWrapper}>
            <InputText
              // onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
              // onBlur={event => onSubmitLeadReporter(dataProviderId, event.target.value)}
              value={leadReporter.account}
            />
          </div>
        );
      }
      return (
        <div className={styles.inputWrapper}>
          <InputText
            // onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
            // onBlur={event => onSubmitLeadReporter(dataProviderId, event.target.value)}
            value={leadReporter.account}
          />
          <Button
            className={`p-button-animated-blink p-button-primary-transparent ${styles.deleteButton}`}
            icon={'trash'}
            // onClick={() => handleDialogs('deleteAttachment', true)}
          />
        </div>
      );
    });
  };

  const providerAccountInputColumnTemplate = representative => {
    let inputData = representative.leadReporters;

    let hasError = formState.representativesHaveError.includes(representative.representativeId);

    const labelId = uuid.v4();

    const onAccountChange = (account, dataProviderId) => {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.dataProviderId === dataProviderId
      );

      thisRepresentative.leadReporters = account;

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
            /*  onBlur={() => {
              //TODO pass to array
              representative.leadReporters = representative.leadReporters[0].toLowerCase();
              isValidEmail(representative.leadReporters) &&
                onAddProvider(formDispatcher, formState, representative, dataflowId);
            }} */
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
        {representative.dataProviderId && (
          <Button
            // className={`p-button-animated-blink p-button-primary-transparent ${styles.deleteButton}`}
            label="Add new lead reporter"
            style={{ display: 'flex' }}
            icon={'plus'}
            disabled={representative.dataProviderId === formState.providerWithEmptyInput}
            onClick={() => onAddEmptyLeadReporter(representative.dataProviderId)}
          />
        )}
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
    </div>
  );
};

export { RepresentativesList };
