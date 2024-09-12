import { Fragment, useContext, useEffect, useMemo, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import uniq from 'lodash/uniq';

import styles from './ManageLeadReporters.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { config } from 'conf';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dropdown } from 'views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';
import ReactTooltip from 'react-tooltip';

import { DataflowService } from 'services/DataflowService';
import { RepresentativeService } from 'services/RepresentativeService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { reducer } from './_functions/Reducers/representativeReducer';

import { isDuplicatedLeadReporter, isValidEmail, parseLeadReporters } from './_functions/Utils/representativeUtils';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageLeadReporters = ({
  dataflowId,
  dataflowType,
  leadReportersDeleted = false,
  representativesImport = false,
  setDataProviderSelected,
  selectedDataProviderGroup,
  setFormHasLeadReporters,
  setHasRepresentativesWithoutDatasets,
  setRepresentativeImport
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    allPossibleDataProvidersNoSelect: [],
    dataProvidersTypesList: [],
    deleteLeadReporterId: null,
    focusedInputId: null,
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

  const { filterBy, filteredData } = useFilters('manageLeadReporters');

  const dataFiltersMemo = useMemo(
    () =>
      formState.representatives.length > formState.allPossibleDataProvidersNoSelect.length
        ? formState.representatives.filter(representative => !isNil(representative.representativeId))
        : formState.representatives,
    [formState.representatives]
  );

  useEffect(() => {
    const representatives = cloneDeep(formState.representatives);
    if (!isEmpty(representatives)) {
      const representativesWithLabel = representatives.map(representative => {
        if (representative.dataProviderId === '') {
          return representative;
        }

        const existRepresentative = formState.allPossibleDataProviders.find(
          item => item.dataProviderId === representative.dataProviderId
        );

        return { ...representative, nameRepresentative: existRepresentative?.label };
      });

      formDispatcher({
        type: 'UPDATE_REPRESENTATIVES_WITH_LABEL',
        payload: { representatives: representativesWithLabel }
      });
    }
  }, [formState.allPossibleDataProviders]);

  useEffect(() => {
    if (representativesImport) {
      getInitialData();
      setRepresentativeImport(false);
    }
  }, [representativesImport]);

  useEffect(() => {
    if (leadReportersDeleted) getInitialData(true);
  }, [leadReportersDeleted]);

  useEffect(() => {
    getInitialData();
  }, [dataflowType, formState.refresher]);

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formState.representatives, formDispatcher);
    }
    setDataProviderSelected(formState.selectedDataProviderGroup);
  }, [formState.selectedDataProviderGroup]);

  useEffect(() => {
    createUnusedOptionsList();
  }, [formState.allPossibleDataProviders]);

  useEffect(() => {
    if (!isEmpty(formState.representatives)) {
      const representativesHasLeadReporters = formState.representatives.filter(
        representative => representative.leadReporters.filter(leadReporter => leadReporter.id !== 'empty').length > 0
      );

      setFormHasLeadReporters(!isEmpty(representativesHasLeadReporters));
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

  const refreshData = () => {
    formDispatcher({ type: 'REFRESH' });
  };

  useCheckNotifications(['VALIDATE_LEAD_REPORTERS_COMPLETED_EVENT'], refreshData);

  const createUnusedOptionsList = () => formDispatcher({ type: 'CREATE_UNUSED_OPTIONS_LIST' });

  const getAllDataProviders = async reportersDeleted => {
    const { representatives, selectedDataProviderGroup } = formState;
    try {
      const responseAllDataProviders = await RepresentativeService.getDataProviders(selectedDataProviderGroup);

      const providersNoSelect = [...responseAllDataProviders];
      if (representatives.length <= responseAllDataProviders.length || reportersDeleted) {
        responseAllDataProviders.unshift({ dataProviderId: '', label: ' Select...' });
      }

      formDispatcher({
        type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
        payload: { responseAllDataProviders, providersNoSelect }
      });
    } catch (error) {
      console.error('RepresentativesList - getAllDataProviders.', error);
    }
  };

  const getDataProviderGroup = async () =>
    formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: selectedDataProviderGroup });

  const getInitialData = async reportersDeleted => {
    try {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: true } });
      switch (dataflowType) {
        case config.dataflowType.BUSINESS.value:
          await getDataProviderGroup();
          break;
        case config.dataflowType.CITIZEN_SCIENCE.value:
          formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: selectedDataProviderGroup });
          await getGroupOrganizations();
          break;
        default:
          await getGroupCountries();
          break;
      }

      await getRepresentatives();
    } catch (error) {
      console.error('RepresentativesList - getInitialData.', error);
    } finally {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: false } });
    }

    if (!isEmpty(formState.representatives)) {
      await getAllDataProviders(reportersDeleted);
      createUnusedOptionsList();
    }
  };

  const getRepresentatives = async () => {
    try {
      const responseRepresentatives = await RepresentativeService.getRepresentatives(dataflowId);
      const parsedLeadReporters = parseLeadReporters(responseRepresentatives.representatives);

      formDispatcher({
        type: 'INITIAL_LOAD',
        payload: { response: responseRepresentatives, parsedLeadReporters }
      });
    } catch (error) {
      console.error('RepresentativesList - getRepresentatives.', error);
      notificationContext.add({ type: 'GET_REPRESENTATIVES_ERROR' }, true);
    }
  };

  const getGroupCountries = async () => {
    try {
      const response = await RepresentativeService.getGroupCountries();
      formDispatcher({ type: 'GET_PROVIDERS_TYPES_LIST', payload: { providerTypes: response.data } });
    } catch (error) {
      console.error('RepresentativesList - getGroupCountries.', error);
    }
  };

  const getGroupOrganizations = async () => {
    try {
      const response = await RepresentativeService.getGroupOrganizations();
      formDispatcher({ type: 'GET_PROVIDERS_TYPES_LIST', payload: { providerTypes: response.data } });
    } catch (error) {
      console.error('RepresentativesList - getGroupOrganizations.', error);
    }
  };

  const handleDialogs = (dialog, isVisible) => {
    formDispatcher({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
  };

  const onAddRepresentative = async representatives => {
    const newRepresentative = representatives.filter(representative => isNil(representative.representativeId));
    if (!isEmpty(newRepresentative[0].dataProviderId)) {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: true } });
      try {
        await RepresentativeService.createDataProvider(
          dataflowId,
          formState.selectedDataProviderGroup.dataProviderGroupId,
          parseInt(newRepresentative[0].dataProviderId)
        );

        formDispatcher({ type: 'REFRESH' });
      } catch (error) {
        console.error('RepresentativesList - onAddRepresentative.', error);
        notificationContext.add({ type: 'ADD_DATA_PROVIDER_ERROR' }, true);
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

  const onDataProviderIdChange = async (newDataProviderId, representative, indexColumn) => {
    if (!isNil(representative.representativeId)) {
      formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: true } });

      try {
        await RepresentativeService.updateDataProviderId(
          parseInt(representative.representativeId),
          parseInt(newDataProviderId)
        );
        formDispatcher({ type: 'REFRESH' });
        if (!isNil(indexColumn) && !isEmpty(formState.representatives)) {
          const representatives = cloneDeep(formState.representatives);
          representatives[indexColumn].dataProviderId = newDataProviderId;
          return representatives;
        }
      } catch (error) {
        console.error('RepresentativesList - onDataProviderIdChange.', error);
        notificationContext.add({ type: 'UPDATE_DATA_PROVIDER_ERROR' }, true);
        formDispatcher({ type: 'SET_IS_LOADING', payload: { isLoading: false } });
        return [];
      }
    } else {
      const representatives = cloneDeep(formState.representatives);
      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.representativeId === representative.representativeId
      );
      thisRepresentative.dataProviderId = newDataProviderId;

      formDispatcher({
        type: 'ON_PROVIDER_CHANGE',
        payload: { representatives }
      });

      return representatives;
    }
  };

  const onDeleteConfirm = async () => {
    setIsDeleting(true);
    try {
      await RepresentativeService.deleteRepresentative(formState.representativeIdToDelete, dataflowId);

      const updatedList = formState.representatives.filter(
        representative => representative.representativeId !== formState.representativeIdToDelete
      );

      formDispatcher({ type: 'DELETE_REPRESENTATIVE', payload: { updatedList } });
    } catch (error) {
      console.error('RepresentativesList - onDeleteConfirm.', error);
      notificationContext.add({ type: 'DELETE_REPRESENTATIVE_ERROR' }, true);
    } finally {
      formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' });
      setIsDeleting(false);
    }
  };

  const onDeleteLeadReporter = async () => {
    setIsDeleting(true);
    try {
      await RepresentativeService.deleteLeadReporter(formState.deleteLeadReporterId, dataflowId);
      formDispatcher({ type: 'REFRESH' });
    } catch (error) {
      console.error('RepresentativesList - onDeleteLeadReporter.', error);
      notificationContext.add({ type: 'DELETE_LEAD_REPORTER_ERROR' }, true);
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
    const hasErrors = true;

    if (!TextUtils.areEquals(inputValue, leadReporter.account)) {
      if (isValidEmail(inputValue) && !isDuplicatedLeadReporter(inputValue, dataProviderId, formState.leadReporters)) {
        try {
          TextUtils.areEquals(leadReporter.id, 'empty')
            ? await RepresentativeService.createLeadReporter(inputValue, representativeId, dataflowId)
            : await RepresentativeService.updateLeadReporter(inputValue, leadReporter.id, representativeId, dataflowId);

          await DataflowService.updateGroupId({
            dataflowId,
            dataProviderGroupId: formState.selectedDataProviderGroup.dataProviderGroupId
          });

          formDispatcher({ type: 'REFRESH' });
        } catch (error) {
          console.error('RepresentativesList - onSubmitLeadReporter.', error);
          onCreateError(dataProviderId, hasErrors, leadReporter.id);
        }
      } else {
        onCreateError(dataProviderId, hasErrors, leadReporter.id);
      }
    }
  };

  const setIsDeleting = value => formDispatcher({ type: 'SET_IS_DELETING', payload: { isDeleting: value } });

  const setFocusedInputId = focusedInputId =>
    formDispatcher({ type: 'SET_FOCUSED_INPUT_ID', payload: { focusedInputId } });

  const filterOptions = [
    {
      type: 'MULTI_SELECT',
      nestedOptions: [
        {
          key: 'nameRepresentative',
          label: resourcesContext.messages['manageRolesDialogDataProviderColumn']
        }
      ]
    },
    {
      type: 'INPUT',
      key: 'leadReporters',
      nestedKey: 'account',
      label: resourcesContext.messages['manageRolesDialogAccountColumn']
    }
  ];

  const renderIsValidUserIcon = (isNewLeadReporter, leadReporter, currentInputId) => {
    if (!isNewLeadReporter && formState.focusedInputId !== currentInputId) {
      return (
        <Fragment>
          <FontAwesomeIcon
            className={styles.isValidUserIcon}
            data-for={leadReporter?.account}
            data-tip
            icon={leadReporter?.isValid ? AwesomeIcons('userCheck') : AwesomeIcons('userTimes')}
          />
          <ReactTooltip border={true} effect="solid" id={leadReporter?.account} place="top">
            {leadReporter?.isValid
              ? resourcesContext.messages['validUserTooltip']
              : resourcesContext.messages['invalidUserTooltip']}
          </ReactTooltip>
        </Fragment>
      );
    }
  };

  const renderLeadReporterColumnTemplate = representative => {
    const { dataProviderId, representativeId } = representative;

    if (isNil(representative.leadReporters)) return [];

    return representative.leadReporters.map(leadReporter => {
      const reporters = formState.leadReporters[dataProviderId];
      const errors = formState.leadReportersErrors[dataProviderId];
      const isNewLeadReporter = TextUtils.areEquals(leadReporter.id, 'empty');
      const uniqueInputId = `${leadReporter.id}-${representativeId}`;
      if (!reporters) {
        return null;
      }

      return (
        <div
          className={`${styles.inputWrapper} ${
            representative.leadReporters.length > 1 ? styles.inputWrapperMargin : undefined
          }`}
          key={uniqueInputId}>
          <InputText
            autoComplete="off"
            autoFocus={isNewLeadReporter && isEmpty(Object.keys(filterBy))}
            className={errors?.[leadReporter.id] ? styles.hasErrors : undefined}
            disabled={representative.hasDatasets && reporters[leadReporter.id]?.isValid}
            id={uniqueInputId}
            onBlur={event => {
              onSubmitLeadReporter(event.target.value, representativeId, dataProviderId, leadReporter);
              setFocusedInputId(null);
            }}
            onChange={event => onChangeLeadReporter(dataProviderId, leadReporter.id, event.target.value)}
            onFocus={() => {
              onCleanErrors(dataProviderId, leadReporter.id);
              setFocusedInputId(uniqueInputId);
            }}
            onKeyDown={event => onKeyDown(event, representativeId, dataProviderId, leadReporter)}
            placeholder={resourcesContext.messages['manageRolesDialogInputPlaceholder']}
            value={reporters[leadReporter.id]?.account || reporters[leadReporter.id]}
          />

          {renderIsValidUserIcon(isNewLeadReporter, reporters[leadReporter.id], uniqueInputId)}

          {!isNewLeadReporter && (
            <Button
              className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton}`}
              icon="trash"
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

  const renderDropdownColumnTemplate = (representative, column) => {
    const selectedOptionForThisSelect = formState.allPossibleDataProviders.filter(
      option => option.dataProviderId === representative.dataProviderId
    );

    const remainingOptionsAndSelectedOption = uniq(
      orderBy(selectedOptionForThisSelect.concat(formState.unusedDataProvidersOptions), ['label'], ['asc']),
      'label'
    );

    const labelId = `${representative.representativeId}-${representative.dataProviderId}`;

    return (
      <Fragment>
        <label className="srOnly" htmlFor={labelId}>
          {resourcesContext.messages['manageRolesDialogInputPlaceholder']}
        </label>
        <select
          className={
            representative.hasDatasets ? `${styles.disabled} ${styles.selectDataProvider}` : styles.selectDataProvider
          }
          disabled={representative.hasDatasets}
          id={labelId}
          onChange={async event => {
            const representatives = await onDataProviderIdChange(event.target.value, representative, column.rowIndex);
            onAddRepresentative(representatives);
          }}
          value={representative.dataProviderId}>
          {remainingOptionsAndSelectedOption.map((provider, i) => (
            <option
              className="p-dropdown-item"
              // eslint-disable-next-line react/no-array-index-key
              key={`${provider.dataProviderId}${provider.label}${i}`}
              value={provider.dataProviderId}>
              {provider.label}
            </option>
          ))}
        </select>
      </Fragment>
    );
  };

  const renderRepresentativesDropdown = () => {
    if (
      TextUtils.areEquals(dataflowType, config.dataflowType.BUSINESS.value) ||
      TextUtils.areEquals(dataflowType, config.dataflowType.CITIZEN_SCIENCE.value)
    ) {
      return (
        <Dropdown
          ariaLabel="dataProviders"
          className={styles.dataProvidersDropdown}
          disabled
          name="dataProvidersDropdown"
          optionLabel="label"
          options={[formState.selectedDataProviderGroup]}
          value={formState.selectedDataProviderGroup}
        />
      );
    }

    return (
      <Dropdown
        ariaLabel="dataProviders"
        className={styles.dataProvidersDropdown}
        disabled={formState.representatives.length > 1}
        name="dataProvidersDropdown"
        onChange={event => formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: event.target.value })}
        optionLabel="label"
        options={
          formState.selectedDataProviderGroup
            ? formState.selectedDataProviderGroup.dataProviderGroupId === 1
              ? formState.dataProvidersTypesList
              : formState.dataProvidersTypesList.filter(countriesGroup => countriesGroup.dataProviderGroupId !== 1)
            : formState.dataProvidersTypesList.filter(countriesGroup => countriesGroup.dataProviderGroupId !== 1)
        }
        placeholder={resourcesContext.messages['manageRolesDialogDropdownPlaceholder']}
        value={formState.selectedDataProviderGroup}
      />
    );
  };

  const renderFilter = () => {
    if (isNil(formState.selectedDataProviderGroup) || isEmpty(formState.allPossibleDataProviders)) {
      return null;
    }

    return (
      <MyFilters className="lineItems" data={dataFiltersMemo} options={filterOptions} viewType="manageLeadReporters" />
    );
  };

  const renderTable = () => {
    if (isNil(formState.selectedDataProviderGroup) || isEmpty(formState.allPossibleDataProviders)) {
      return (
        <p className={styles.chooseRepresentative}>
          {resourcesContext.messages['manageRolesDialogNoRepresentativesMessage']}
        </p>
      );
    }

    if (isEmpty(filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noLeadReportersWithSelectedParameters']}
        </div>
      );
    }

    return (
      <Fragment>
        {formState.isLoading && <Spinner className={styles.spinner} />}
        <DataTable value={filteredData}>
          <Column
            body={renderDeleteBtnColumnTemplate}
            className={styles.emptyTableHeader}
            header={resourcesContext.messages['deleteRepresentativeButtonTableHeader']}
            style={{ width: '60px' }}
          />
          <Column
            body={renderDropdownColumnTemplate}
            header={resourcesContext.messages['manageRolesDialogDataProviderColumn']}
            style={{ width: '16rem' }}
          />
          <Column
            body={renderLeadReporterColumnTemplate}
            header={resourcesContext.messages['manageRolesDialogAccountColumn']}
          />
        </DataTable>
      </Fragment>
    );
  };

  const renderDeleteBtnColumnTemplate = representative => {
    if (!isNil(representative.representativeId) && !representative.hasDatasets) {
      return (
        <ActionsColumn
          onDeleteClick={() => {
            formDispatcher({
              type: 'SHOW_CONFIRM_DIALOG',
              payload: { representativeId: representative.representativeId }
            });
          }}
        />
      );
    }
  };

  if (isEmpty(formState.representatives)) {
    return <Spinner style={{ top: 0 }} />;
  }

  return (
    <div className={styles.container}>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>{resourcesContext.messages['manageRolesDialogHeader']}</div>
        <div>
          <label>{resourcesContext.messages['manageRolesDialogDropdownLabel']} </label>
          {renderRepresentativesDropdown()}
        </div>
      </div>
      {renderFilter()}
      {renderTable()}
      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={formState.isDeleting}
          header={resourcesContext.messages['manageRolesDialogConfirmDeleteProviderHeader']}
          iconConfirm={formState.isDeleting ? 'spinnerAnimate' : undefined}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteConfirm}
          onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
          visible={formState.isVisibleConfirmDeleteDialog}>
          <p>{resourcesContext.messages['manageRolesDialogConfirmDeleteProviderQuestion']}</p>
        </ConfirmDialog>
      )}
      {isVisibleDialog.deleteLeadReporter && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={formState.isDeleting}
          header={resourcesContext.messages['manageRolesDialogConfirmDeleteHeader']}
          iconConfirm={formState.isDeleting ? 'spinnerAnimate' : undefined}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteLeadReporter}
          onHide={() => {
            handleDialogs('deleteLeadReporter', false);
            formDispatcher({ type: 'LEAD_REPORTER_DELETE_ID', payload: { id: null } });
          }}
          visible={isVisibleDialog.deleteLeadReporter}>
          <p>{resourcesContext.messages['manageRolesDialogConfirmDeleteQuestion']}</p>
        </ConfirmDialog>
      )}
    </div>
  );
};
