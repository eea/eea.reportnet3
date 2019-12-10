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

  const namesList = [
    { nameLabel: 'Read only', name: 'read' },
    { nameLabel: 'Read/Write', name: 'read_write' }
  ];

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

  const initialState = { name: '', dataProviderId: '' };

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
        newState = { name: 'read', dataProviderId: action.payload };

        onDataProviderRoleUpdate(newState.dataProviderId, newState.name);

        return newState;

      case 'UPDATE_TO_READ_WRITE':
        newState = { name: 'read_write', dataProviderId: action.payload };

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
    const getActualRole = () => {
      if (rowData) {
        switch (rowData.name) {
          case 'read':
            return { nameLabel: 'Read only', name: 'read' };

          case 'read_write':
            return { nameLabel: 'Read/Write', name: 'read_write' };

          default:
            return { nameLabel: '', name: '' };
        }
      }
    };

    return (
      <>
        <Dropdown
          optionLabel="nameLabel"
          value={getActualRole()}
          options={namesList}
          placeholder={resources.messages.selectDataProviderRole}
          onChange={e => {
            dataProviderDispatcher({ type: `UPDATE_TO_${e.value.name}`.toUpperCase(), payload: rowData.id });
          }}
        />
      </>
    );
  };

  const deleteBtnColumnTemplate = rowData => {
    return (
      <>
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
      </>
    );
  };

  const actualDataProvidersLoginsList = dataProviders.map(dataProvider => dataProvider.email);

  const addDataProviderValidationSchema = Yup.object().shape({
    addDataProviderLogin: Yup.string()
      .min(6, resources.messages.dataProviderLoginValidationMin)
      .required(' ')
      .notOneOf(actualDataProvidersLoginsList, ' '),
    newDataProviderRole: Yup.string().required(' ')
  });

  return (
    <>
      <div>
        {/*   <Formik
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
                  {namesList.map(name => (
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
        /> */}
      </div>

      <DataTable value={dataProviders} paginator={false} scrollable={true} scrollHeight="60vh">
        <Column key="email" field="email" header="Login" />
        <Column body={nameDropdownColumnTemplate} header="Role" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
    </>
  );
};

export { DataProvidersList };
