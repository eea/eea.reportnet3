import React, { useContext, useEffect, useState, useReducer } from 'react';
import { Formik, Field, Form } from 'formik';
import * as Yup from 'yup';

import { isEmpty } from 'lodash';

import styles from './DataProvidersList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  const [dataProviders, setDataProviders] = useState([]);
  const [selectedDataProvidersType, setSelectedDataProvidersType] = useState(null);
  const [representativeList, setRepresentativeList] = useState([]);

  useEffect(() => {
    //Need get function on api for representatives list
    // Http requester......
    setRepresentativeList([
      { nameLabel: 'Countries', name: 'countries' },
      { nameLabel: 'Companies', name: 'companies' }
    ]);
  }, []);

  const onSelectProvidersType = e => {
    setSelectedDataProvidersType(e.value);
  };

  const getDataProvidersListOfSelectedType = async type => {
    setDataProviders(await DataProviderService.allProviders(dataflowId));
  };

  const loadDataProvidersList = async () => {
    setDataProviders(await DataProviderService.all(dataflowId));
  };

  useEffect(() => {
    loadDataProvidersList();
  }, []);

  const onDataProviderRoleUpdate = async (dataProviderId, newRole) => {
    await DataProviderService.update(dataflowId, dataProviderId, newRole);
  };

  const onDataProviderAdd = async (email, name) => {
    await DataProviderService.add(dataflowId, email, name);
  };

  const onDataProviderDelete = async dataProviderId => {
    await DataProviderService.deleteById(dataflowId, dataProviderId);
  };

  const initialState = { name: '', email: '', dataProviderId: '' };

  const nameReducer = (state, action) => {
    let newState;
    switch (action.type) {
      case 'ADD_DATAPROVIDER':
        newState = { ...state, name: action.payload.name, email: action.payload.email };

        onDataProviderAdd(newState.email, newState.name);

        return newState;

      case 'DELETE_DATAPROVIDER':
        newState = { ...state, name: '', dataProviderId: action.payload };

        onDataProviderDelete(newState.dataProviderId);

        return newState;

      case 'UPDATE_TO_READ':
        newState = { name: 'countries', dataProviderId: action.payload };

        onDataProviderRoleUpdate(newState.dataProviderId, newState.name);

        return newState;

      case 'UPDATE_TO_READ_WRITE':
        newState = { name: 'companies', dataProviderId: action.payload };

        onDataProviderRoleUpdate(newState.dataProviderId, newState.name);

        return newState;

      default:
        return state;
    }
  };
  const [dataProviderState, dataProviderDispatcher] = useReducer(nameReducer, initialState);

  useEffect(() => {
    loadDataProvidersList();
  }, [dataProviderState]);

  const nameDropdownColumnTemplate = rowData => {
    const getActualName = () => {
      if (rowData) {
        switch (rowData.name) {
          case 'read':
            return { nameLabel: 'Read only', name: 'read' };

          case 'read_write':
            return { nameLabel: 'Read/Write', name: 'read_write' };

          case 'countries':
            return { nameLabel: 'Countries', name: 'countries' };

          case 'companies':
            return { nameLabel: 'Companies', name: 'companies' };

          default:
            return { nameLabel: '', name: '' };
        }
      }
    };

    return (
      <Dropdown
        optionLabel="nameLabel"
        value={getActualName()}
        options={representativeList}
        placeholder={resources.messages.selectDataProviderRole}
        onChange={e => {
          dataProviderDispatcher({ type: `UPDATE_TO_${e.value.name}`.toUpperCase(), payload: rowData.id });
        }}
      />
    );
  };

  const deleteBtnColumnTemplate = rowData => {
    return (
      <Button
        tooltip={resources.messages.deleteDataProvider}
        tooltipOptions={{ position: 'right' }}
        icon="trash"
        disabled={false}
        className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
        onClick={e => {
          dataProviderDispatcher({ type: 'DELETE_DATAPROVIDER', payload: rowData.id });
        }}
      />
    );
  };

  const getDataProvidersList = providerType => {};

  /* const actualDataProvidersLoginsList = dataProviders.map(dataProvider => dataProvider.email); */

  /*  const addDataProviderValidationSchema = Yup.object().shape({
    addDataProviderLogin: Yup.string()
      .min(6, resources.messages.dataProviderLoginValidationMin)
      .required(' ')
      .notOneOf(actualDataProvidersLoginsList, ' '),
    newDataProviderRole: Yup.string().required(' ')
  }); */

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>Data providers</div>

        <div>
          <label htmlFor="selectedDataProvidersType">Representative of </label>

          <Dropdown
            name="selectedDataProvidersType"
            optionLabel="nameLabel"
            value={selectedDataProvidersType}
            options={representativeList}
            placeholder={'Choose...'}
            onChange={onSelectProvidersType}
          />
        </div>
      </div>

      <DataTable value={dataProviders} paginator={false} scrollable={true} scrollHeight="60vh">
        <Column key="email" field="email" header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
    </>
  );
};

export { DataProvidersList };

{
  /*   <Formik
          initialValues={{ addDataProviderLogin: '' }}
          validationSchema={addDataProviderValidationSchema}
          onSubmit={e => {
            dataProviderDispatcher({
              type: 'ADD_DATAPROVIDER',
              payload: { email: e.addDataProviderLogin, name: e.newDataProviderRole }
            });
            e.addDataProviderLogin = '';
          }}
          render={({ errors, touched, isSubmitting }) => (
            <Form className={styles.addDataProviderWrapper}>
              <div
                className={` formField ${
                  !isEmpty(errors.addDataProviderLogin) && touched.addDataProviderLogin ? ' error' : ''
                }`}>
                <Field
                  type="text"
                  name="addDataProviderLogin"
                  placeholder={resources.messages.addDataProviderPlaceholder}
                />
                {errors.addDataProviderLogin || touched.addDataProviderLogin ? (
                  <div className="error">{errors.addDataProviderLogin}</div>
                ) : null}
              </div>
              <div className={` formField ${!isEmpty(errors.newDataProviderRole) ? ' error' : ''}`}>
                <Field name="newDataProviderRole" component="select">
                  <option value="">{resources.messages.selectDataProviderRole}</option>
                  {representativesList.map(name => (
                    <option key={name.nameLabel} value={name.name}>
                      {name.nameLabel}
                    </option>
                  ))}
                </Field>
                {errors.newDataProviderRole || touched.newDataProviderRole ? (
                  <div className="error">{errors.newDataProviderRole}</div>
                ) : null}
              </div>
              <div className="buttonWrapper">
                <Button
                  type="submit"
                  icon="plus"
                  tooltip={resources.messages.addDataProvider}
                  tooltipOptions={{ position: 'right' }}
                  label={resources.messages.add}
                  className={`${styles.addDataProviderButton} rp-btn default`}
                />
              </div>
            </Form>
          )}
        /> */
}
