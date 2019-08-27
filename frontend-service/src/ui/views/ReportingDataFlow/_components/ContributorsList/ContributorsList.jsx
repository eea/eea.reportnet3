import React, { useContext, useEffect, useState, useReducer } from 'react';
import { Formik, Field, Form } from 'formik';
import * as Yup from 'yup';

import { isEmpty } from 'lodash';

import styles from './ContributorsList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';

import { ContributorService } from 'core/services/Contributor';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export function ContributorsList({ dataFlowId }) {
  const resources = useContext(ResourcesContext);
  const [contributorsArray, setContributorsArray] = useState([]);

  const rolesList = [{ roleLabel: 'Read only', role: 'read' }, { roleLabel: 'Read/Write', role: 'read_write' }];

  const loadContributorsList = async () => {
    setContributorsArray(await ContributorService.all(dataFlowId));
  };

  useEffect(() => {
    loadContributorsList();
  }, []);

  const onContributorRoleUpdate = async (contributorId, newRole) => {
    await ContributorService.updateById(dataFlowId, contributorId, newRole);
  };

  const onContributorAdd = async (login, role) => {
    await ContributorService.addByLogin(dataFlowId, login, role);
  };

  const onContributorDelete = async contributorId => {
    await ContributorService.deleteById(dataFlowId, contributorId);
  };
  /* #region Actions Reducer */
  const initialState = { role: '', contributorId: '' };

  const roleReducer = (state, action) => {
    let newState;
    switch (action.type) {
      case 'ADD_CONTRIBUTOR':
        newState = { ...state, role: action.payload.role, login: action.payload.login };

        onContributorAdd(newState.login, newState.role);

        return newState;

      case 'DELETE_CONTRIBUTOR':
        newState = { ...state, role: '', contributorId: action.payload };

        onContributorDelete(newState.contributorId);

        return newState;

      case 'UPDATE_TO_READ':
        newState = { role: 'read', contributorId: action.payload };

        onContributorRoleUpdate(newState.contributorId, newState.role);

        return newState;

      case 'UPDATE_TO_READ_WRITE':
        newState = { role: 'read_write', contributorId: action.payload };

        onContributorRoleUpdate(newState.contributorId, newState.role);

        return newState;

      default:
        return state;
    }
  };
  const [contributorState, contributorDispatcher] = useReducer(roleReducer, initialState);
  /* #endregion */

  /* #region ROLES */
  useEffect(() => {
    loadContributorsList();
  }, [contributorState]);

  const roleDropdownColumnTemplate = rowData => {
    const getActualRole = () => {
      if (rowData) {
        switch (rowData.role) {
          case 'read':
            return { roleLabel: 'Read only', role: 'read' };

          case 'read_write':
            return { roleLabel: 'Read/Write', role: 'read_write' };

          default:
            return { roleLabel: '', role: '' };
        }
      }
    };

    return (
      <>
        <Dropdown
          optionLabel="roleLabel"
          value={getActualRole()}
          options={rolesList}
          placeholder={resources.messages.selectContributorRole}
          onChange={e => {
            contributorDispatcher({ type: `UPDATE_TO_${e.value.role}`.toUpperCase(), payload: rowData.id });
          }}
        />
      </>
    );
  };
  /* #endregion */

  /* #region DELETE */
  const deleteBtnColumnTemplate = rowData => {
    return (
      <>
        <Button
          tooltip={resources.messages.deleteContributor}
          tooltipOptions={{ position: 'left' }}
          icon="trash"
          disabled={false}
          className={`${styles.btn} rp-btn warning`}
          onClick={e => {
            contributorDispatcher({ type: 'DELETE_CONTRIBUTOR', payload: rowData.id });
          }}
        />
      </>
    );
  };
  /* #endregion */

  /* #region ADD */
  const actualContributorsLoginsList = contributorsArray.map(contributor => contributor.login);

  const addContributorValidationSchema = Yup.object().shape({
    addContributorLogin: Yup.string()
      .min(6, resources.messages.contributorLoginValidationMin)
      .max(50, resources.messages.contributorLoginValidationMax)
      .required(resources.messages.contributorLoginValidationRequired)
      .notOneOf(actualContributorsLoginsList, resources.messages.contributorLoginValidationIsInList),
    newContributorRole: Yup.string().required(resources.messages.contributorRoleValidationIsRequired)
  });
  /* #endregion */

  return (
    <>
      <div>
        <Formik
          initialValues={{ addContributorLogin: '' }}
          validationSchema={addContributorValidationSchema}
          onSubmit={e => {
            contributorDispatcher({
              type: 'ADD_CONTRIBUTOR',
              payload: { login: e.addContributorLogin, role: e.newContributorRole }
            });
            e.addContributorLogin = '';
          }}
          render={({ errors, touched, isSubmitting }) => (
            <Form className={styles.addContributorWrapper}>
              <div
                className={` formField ${
                  !isEmpty(errors.addContributorLogin) && touched.addContributorLogin ? ' error' : ''
                }`}>
                <Field
                  type="text"
                  name="addContributorLogin"
                  placeholder={resources.messages.addContributorPlaceholder}
                />
                {errors.addContributorLogin || touched.addContributorLogin ? (
                  <div className="error">{errors.addContributorLogin}</div>
                ) : null}
              </div>
              <div className={` formField ${!isEmpty(errors.newContributorRole) ? ' error' : ''}`}>
                <Field name="newContributorRole" component="select">
                  <option value="">{resources.messages.selectContributorRole}</option>
                  {rolesList.map(role => (
                    <option key={role.roleLabel} value={role.role}>
                      {role.roleLabel}
                    </option>
                  ))}
                </Field>
                {errors.newContributorRole || touched.newContributorRole ? (
                  <div className="error">{errors.newContributorRole}</div>
                ) : null}
              </div>
              <div className="buttonWrapper">
                <Button
                  type="submit"
                  icon="plus"
                  tooltip={resources.messages.addContributor}
                  tooltipOptions={{ position: 'left' }}
                  label={resources.messages.add}
                  className={`${styles.addContributorButton} rp-btn default`}
                />
              </div>
            </Form>
          )}
        />
      </div>
      <DataTable value={contributorsArray} paginator={true} rows={5} rowsPerPageOptions={[4, 6, 8]}>
        <Column key="login" field="login" header="Login" />
        <Column body={roleDropdownColumnTemplate} header="Role" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
    </>
  );
}
