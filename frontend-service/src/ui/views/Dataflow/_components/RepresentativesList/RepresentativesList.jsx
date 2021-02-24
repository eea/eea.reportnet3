import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import uuid from 'uuid';

import styles from './RepresentativesList.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { RepresentativeService } from 'core/services/Representative';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { reducer } from './_functions/Reducers/representativeReducer.js';

import { isValidEmail, parseLeadReporters } from './_functions/Utils/representativeUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const RepresentativesList = ({ dataflowId, setFormHasRepresentatives, setHasRepresentativesWithoutDatasets }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    allPossibleDataProvidersNoSelect: [],
    dataProvidersTypesList: [],
    deleteLeadReporterId: null,
    isDeleting: false,
    isLoading: false,
    isVisibleConfirmDeleteDialog: false,
    isVisibleDialog: { deleteLeadReporter: false, deleteRepresentative: false },
    leadReporters: {},
    leadReportersErrors: {},
    refresher: false,
    representativeIdToDelete: '',
    representatives: [],
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  const { isVisibleDialog } = formState;

  useEffect(() => {
    getInitialData();
  }, [formState.refresher]);

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formState.representatives, formDispatcher);
    }
  }, [formState.selectedDataProviderGroup]);

  useEffect(() => {
    createUnusedOptionsList();
  }, [formState.allPossibleDataProviders]);

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

  const createUnusedOptionsList = () => formDispatcher({ type: 'CREATE_UNUSED_OPTIONS_LIST' });

  const getAllDataProviders = async () => {
    const { representatives, selectedDataProviderGroup } = formState;
    try {
      const responseAllDataProviders = await RepresentativeService.allDataProviders(selectedDataProviderGroup);

      const providersNoSelect = [...responseAllDataProviders];
      if (representatives.length <= responseAllDataProviders.length) {
        responseAllDataProviders.unshift({ dataProviderId: '', label: ' Select...' });
      }

      formDispatcher({
        type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
        payload: { responseAllDataProviders, providersNoSelect }
      });
    } catch (error) {
      console.error('error on RepresentativeService.allDataProviders', error);
    }
  };

  const getInitialData = async () => {
    await getProviderTypes();
    await getAllRepresentatives();

    if (!isEmpty(formState.representatives)) {
      await getAllDataProviders();
      createUnusedOptionsList();
    }
  };

  const getAllRepresentatives = async () => {
    try {
      let responseAllRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);

      const parsedLeadReporters = parseLeadReporters(responseAllRepresentatives.representatives);

      formDispatcher({
        type: 'INITIAL_LOAD',
        payload: { response: responseAllRepresentatives, parsedLeadReporters }
      });
    } catch (error) {
      console.error('error on RepresentativeService.allRepresentatives', error);
      notificationContext.add({ type: 'GET_REPRESENTATIVES_ERROR' });
    }
  };

  const getProviderTypes = async () => {
    try {
      const providerTypes = await RepresentativeService.getProviderTypes();
      formDispatcher({ type: 'GET_PROVIDERS_TYPES_LIST', payload: { providerTypes } });
    } catch (error) {
      console.error('error on  RepresentativeService.getProviderTypes', error);
    }
  };

  const handleDialogs = (dialog, isVisible) => {
    formDispatcher({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
  };

  const onAddRepresentative = async () => {
    const { representatives } = formState;

    const newRepresentative = representatives.filter(representative => isNil(representative.representativeId));
    if (!isEmpty(newRepresentative[0].dataProviderId)) {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: true } });
      try {
        await RepresentativeService.add(
          dataflowId,
          formState.selectedDataProviderGroup.dataProviderGroupId,
          parseInt(newRepresentative[0].dataProviderId)
        );

        formDispatcher({ type: 'REFRESH' });
      } catch (error) {
        console.error('error on RepresentativeService.add', error);
        notificationContext.add({ type: 'ADD_DATA_PROVIDER_ERROR' });
      } finally {
        formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: false } });
      }
    }
  };

  const onChangeLeadReporter = async (dataProviderId, leadReporterId, inputValue) => {
    formDispatcher({ type: 'ON_CHANGE_LEAD_REPORTER', payload: { dataProviderId, leadReporterId, inputValue } });
  };

  const onCleanErrors = (dataProviderId, leadReporterId) => {
    formDispatcher({ type: 'CLEAN_UP_ERRORS', payload: { dataProviderId, hasErrors: false, leadReporterId } });
  };

  const onCreateError = (dataProviderId, hasErrors, leadReporterId) => {
    formDispatcher({ type: 'CREATE_ERROR', payload: { dataProviderId, hasErrors, leadReporterId } });
  };

  const onDataProviderIdChange = async (newDataProviderId, representative) => {
    if (!isNil(representative.representativeId)) {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: true } });

      try {
        await RepresentativeService.updateDataProviderId(
          parseInt(representative.representativeId),
          parseInt(newDataProviderId)
        );
        formDispatcher({ type: 'REFRESH' });
      } catch (error) {
        console.error('error on RepresentativeService.updateDataProviderId', error);
        notificationContext.add({ type: 'UPDATE_DATA_PROVIDER_ERROR' });
      } finally {
        formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: false } });
      }
    } else {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.representativeId === representative.representativeId
      );
      thisRepresentative.dataProviderId = newDataProviderId;

      formDispatcher({ type: 'ON_PROVIDER_CHANGE', payload: { representatives } });
    }
  };

  const onDeleteConfirm = async () => {
    setIsDeleting(true);
    try {
      await RepresentativeService.deleteById(formState.representativeIdToDelete);

      const updatedList = formState.representatives.filter(
        representative => representative.representativeId !== formState.representativeIdToDelete
      );

      formDispatcher({ type: 'DELETE_REPRESENTATIVE', payload: { updatedList } });
    } catch (error) {
      console.error('error on RepresentativeService.deleteById: ', error);
      notificationContext.add({ type: 'DELETE_REPRESENTATIVE_ERROR' });
    } finally {
      formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' });
      setIsDeleting(false);
    }
  };

  const onDeleteLeadReporter = async () => {
    setIsDeleting(true);
    try {
      const response = await RepresentativeService.deleteLeadReporter(formState.deleteLeadReporterId);

      if (response.status >= 200 && response.status <= 299) {
        formDispatcher({ type: 'REFRESH' });
      }
    } catch (error) {
      console.error('error on RepresentativeService.deleteLeadReporter', error);
      notificationContext.add({ type: 'DELETE_LEAD_REPORTER_ERROR' });
    } finally {
      handleDialogs('deleteLeadReporter', false);
      setIsDeleting(false);
    }
  };

  const onKeyDown = (event, representativeId, dataProviderId, leadReporter) => {
    if (TextUtils.areEquals(event.key, 'Enter')) {
      onSubmitLeadReporter(event.target.value, representativeId, dataProviderId, leadReporter);
    }
  };

  const onSubmitLeadReporter = async (inputValue, representativeId, dataProviderId, leadReporter) => {
    const { addLeadReporter, updateLeadReporter } = RepresentativeService;
    const hasErrors = true;

    if (!TextUtils.areEquals(inputValue, leadReporter.account)) {
      if (isValidEmail(inputValue)) {
        try {
          const response = TextUtils.areEquals(leadReporter.id, 'empty')
            ? await addLeadReporter(inputValue, representativeId)
            : await updateLeadReporter(inputValue, leadReporter.id, representativeId);

          if (response.status >= 200 && response.status <= 299) {
            formDispatcher({ type: 'REFRESH' });
          }
        } catch (error) {
          console.error('error on RepresentativeService.addLeadReporter', error);

          onCreateError(dataProviderId, hasErrors, leadReporter.id);
        }
      } else {
        onCreateError(dataProviderId, hasErrors, leadReporter.id);
      }
    }
  };

  const setIsDeleting = value => formDispatcher({ type: 'SET_IS_DELETING', payload: { isDeleting: value } });

  const renderLeadReporterColumnTemplate = representative => {
    const { dataProviderId, representativeId } = representative;

    if (isNil(representative.leadReporters)) return [];

    return representative.leadReporters.map(leadReporter => {
      const reporters = formState.leadReporters[dataProviderId];
      const errors = formState.leadReportersErrors[dataProviderId];
      const isNewLeadReporter = TextUtils.areEquals(leadReporter.id, 'empty');

      return (
        <div className={styles.inputWrapper} key={`${leadReporter.id}-${representativeId}`}>
          <InputText
            className={errors?.[leadReporter.id] ? styles.hasErrors : undefined}
            id={`${leadReporter.id}-${representativeId}`}
            onBlur={event => onSubmitLeadReporter(event.target.value, representativeId, dataProviderId, leadReporter)}
            onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
            onFocus={() => onCleanErrors(dataProviderId, leadReporter.id)}
            onKeyDown={event => onKeyDown(event, representativeId, dataProviderId, leadReporter)}
            placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
            value={reporters[leadReporter.id]?.account || reporters[leadReporter.id]}
          />

          {!isNewLeadReporter && (
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton}`}
              icon={'trash'}
              onClick={() => {
                handleDialogs('deleteLeadReporter', true);
                formDispatcher({ type: 'LEAD_REPORTER_DELETE_ID', payload: { id: leadReporter.id } });
              }}
            />
          )}
        </div>
      );
    });
  };

  const renderDropdownColumnTemplate = representative => {
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
          autoFocus
          className={
            representative.hasDatasets ? `${styles.disabled} ${styles.selectDataProvider}` : styles.selectDataProvider
          }
          disabled={representative.hasDatasets}
          id={labelId}
          onBlur={() => onAddRepresentative()}
          onChange={event => onDataProviderIdChange(event.target.value, representative)}
          onKeyDown={event => {
            if (TextUtils.areEquals(event.key, 'Enter')) {
              onAddRepresentative();
            }
          }}
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

  const renderDeleteBtnColumnTemplate = representative => {
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
              body={renderDeleteBtnColumnTemplate}
              className={styles.emptyTableHeader}
              header={resources.messages['deleteRepresentativeButtonTableHeader']}
              style={{ width: '60px' }}
            />
            <Column
              body={renderDropdownColumnTemplate}
              header={resources.messages['manageRolesDialogDataProviderColumn']}
              style={{ width: '16rem' }}
            />
            <Column
              body={renderLeadReporterColumnTemplate}
              header={resources.messages['manageRolesDialogAccountColumn']}
            />
          </DataTable>
        </div>
      ) : (
        <p className={styles.chooseRepresentative}>{resources.messages['manageRolesDialogNoRepresentativesMessage']}</p>
      )}

      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={formState.isDeleting}
          header={resources.messages['manageRolesDialogConfirmDeleteProviderHeader']}
          iconConfirm={formState.isDeleting ? 'spinnerAnimate' : undefined}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteConfirm()}
          onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
          visible={formState.isVisibleConfirmDeleteDialog}>
          {resources.messages['manageRolesDialogConfirmDeleteProviderQuestion']}
        </ConfirmDialog>
      )}

      {isVisibleDialog.deleteLeadReporter && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={formState.isDeleting}
          header={resources.messages['manageRolesDialogConfirmDeleteHeader']}
          iconConfirm={formState.isDeleting ? 'spinnerAnimate' : undefined}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteLeadReporter()}
          onHide={() => {
            handleDialogs('deleteLeadReporter', false);
            formDispatcher({ type: 'LEAD_REPORTER_DELETE_ID', payload: { id: null } });
          }}
          visible={isVisibleDialog.deleteLeadReporter}>
          {resources.messages['manageRolesDialogConfirmDeleteQuestion']}
        </ConfirmDialog>
      )}
    </div>
  );
};

export { RepresentativesList };
