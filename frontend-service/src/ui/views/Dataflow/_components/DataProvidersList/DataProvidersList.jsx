import React, { useContext, useEffect, useReducer } from 'react';

import styles from './DataProvidersList.module.scss';

import { reducer } from './_components/reducer.jsx';
import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    isVisibleConfirmDeleteDialog: false,
    dataProviderIdToDelete: '',
    dataProviders: [],
    allPossibleDataProviders: [],
    unusedDataProvidersOptions: [],
    representativesTypesList: [],
    representativesDropdown: null,
    selectedRepresentativeType: null
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  const onPageLoad = () => {
    const loadedData = {
      representativesOf: { label: 'Countries', dataProviderGroupId: 123456 },
      dataProviders: [
        { representativeId: 11, dataProviderId: 1, providerAccount: 'spain@es.es' },
        { representativeId: 22, dataProviderId: 2, providerAccount: 'germany@de.de' },
        { representativeId: 33, dataProviderId: 3, providerAccount: 'greatbr@uk.uk' }
      ]
    };

    return loadedData;
  };

  useEffect(() => {
    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: onPageLoad()
    });
  }, []);

  useEffect(() => {
    formDispatcher({
      type: 'GET_REPRESENTATIVES_TYPES_LIST'
    });

    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_TYPE'
    });

    formDispatcher({
      type: 'CREATE_UNUSED_OPTIONS_LIST'
    });
  }, []);

  const providerAccountInputColumnTemplate = rowData => {
    let inputData = rowData.providerAccount;
    return (
      <input
        defaultValue={inputData}
        placeholder={'Data Providers providerAccount...'}
        onChange={e =>
          formDispatcher({
            type: 'ON_EMAIL_CHANGE',
            payload: { input: e.target.value, dataProviderId: rowData.dataProviderId }
          })
        }
        onBlur={e =>
          formDispatcher({
            type: 'UPDATE_EMAIL',
            payload: { input: e.target.value, dataProviderId: rowData.dataProviderId }
          })
        }
      />
    );
  };

  const nameDropdownColumnTemplate = rowData => {
    const selectedOptionForThisSelect = formState.allPossibleDataProviders.filter(
      option => option.dataProviderId === rowData.dataProviderId
    );

    const remainingOptionsAndSelectedOption = selectedOptionForThisSelect.concat(formState.unusedDataProvidersOptions);

    return (
      <>
        <select
          className="p-dropdown p-component"
          onChange={e => {
            formDispatcher({
              type: 'ON_PROVIDER_CHANGE',
              payload: { dataProviderId: e.target.value, representativeId: rowData.representativeId }
            });
          }}
          value={rowData.dataProviderId}>
          {remainingOptionsAndSelectedOption.map(provider => {
            return (
              <option
                key={provider.dataProviderId}
                className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
                value={provider.dataProviderId}>
                {provider.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = rowData => {
    return rowData.dataProviderId !== '' ? (
      <Button
        tooltip={resources.messages.deleteDataProvider}
        tooltipOptions={{ position: 'right' }}
        icon="trash"
        disabled={false}
        className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
        onClick={() => {
          formDispatcher({ type: 'SHOW_CONFIRM_DIALOG', payload: { representativeId: rowData.representativeId } });
        }}
      />
    ) : (
      <></>
    );
  };

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>Data providers</div>

        <div>
          <label htmlFor="representativesDropdown">Representative of </label>

          <Dropdown
            disabled={
              formState.selectedRepresentativeType !== null && formState.representativesTypesList !== [] ? true : false
            }
            name="representativesDropdown"
            optionLabel="label"
            placeholder="Select..."
            value={formState.selectedRepresentativeType}
            options={formState.representativesTypesList}
            onChange={e => {
              return formDispatcher({ type: 'SELECT_REPRESENTATIVE_TYPE', payload: e.target.value });
            }}
          />
        </div>
      </div>

      <DataTable value={formState.dataProviders} scrollable={true} scrollHeight="100vh">
        <Column body={providerAccountInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
      <ConfirmDialog
        onConfirm={() =>
          formDispatcher({
            type: 'DELETE_DATA_PROVIDER',
            payload: { dataProviderId: formState.dataProviderIdToDelete }
          })
        }
        onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
        visible={formState.isVisibleConfirmDeleteDialog}
        header={'Delete data provider'}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {'Do you really want to delete this data provider?'}
      </ConfirmDialog>
    </>
  );
};

export { DataProvidersList };
