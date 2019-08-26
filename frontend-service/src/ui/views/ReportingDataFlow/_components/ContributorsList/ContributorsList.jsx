import React, { useContext, useEffect, useState, useReducer } from 'react';

import styles from './ContributorsList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';
import { Contributor } from './_components/Contributor';

import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';

import { ContributorService } from 'core/services/Contributor';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export function ContributorsList({ dataFlowId }) {
  const resources = useContext(ResourcesContext);
  const [contributorsArray, setContributorsArray] = useState([]);

  const onLoadContributorsList = async () => {
    setContributorsArray(await ContributorService.all(dataFlowId));
  };

  useEffect(() => {
    onLoadContributorsList();
  }, []);

  const deleteBtnColumnTemplate = rowData => {
    return (
      <>
        <Button
          tooltip={resources.messages.deleteContributor}
          tooltipOptions={{ position: 'left' }}
          icon="trash"
          disabled={false}
          className={`${styles.btn} rp-btn warning`}
          onClick={() => {}}
        />
      </>
    );
  };
  //Reducer for role cell controll
  const initialState = { role: '', contributorId: '' };

  const onContributorRoleUpdate = async (contributorId, newRole) => {
    await ContributorService.updateById(dataFlowId, contributorId, newRole);
  };
  const roleReducer = (state, action) => {
    let newState;
    switch (action.type) {
      case 'read':
        onContributorRoleUpdate(action.payload, 'read');
        /*  newState = { ...state, role: 'read', contributorId: action.payload };
        console.log('roleState read:', newState); */
        return newState;
      case 'read_write':
        newState = { ...state, role: 'read_write', contributorId: action.payload };
        console.log('roleState read_write:', newState);
        return newState;

      default:
        return state;
    }
  };
  const [roleState, roleDispatcher] = useReducer(roleReducer, initialState);
  //End Reducer for role cell controll

  const onRoleChange = async () => {
    setContributorsArray(await ContributorService.all(dataFlowId));
  };
  useEffect(() => {
    onRoleChange();
  }, [roleState]);

  const roleDropdownColumnTemplate = rowData => {
    const rolesList = [{ roleLabel: 'Read only', role: 'read' }, { roleLabel: 'Read/Write', role: 'read_write' }];

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
            roleDispatcher({ type: e.value.role, payload: rowData.id });
          }}
        />
      </>
    );
  };

  return (
    <>
      <DataTable value={contributorsArray} paginator={true} rows={5} rowsPerPageOptions={[4, 6, 8]}>
        <Column key="login" field="login" header="Login" />
        <Column body={roleDropdownColumnTemplate} header="Role" />
        <Column body={deleteBtnColumnTemplate} />
      </DataTable>
      <Button
        icon="plus"
        tooltip={resources.messages.addContributor}
        label={resources.messages.add}
        className={`${styles.addContributorButton} rp-btn default`}
      />
    </>
  );
}
