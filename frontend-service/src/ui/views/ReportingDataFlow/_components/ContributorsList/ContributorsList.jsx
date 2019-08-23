import React, { useContext, useEffect, useState } from 'react';

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
      <div>
        <Button
          tooltip={resources.messages.deleteContributor}
          tooltipOptions={{ position: 'left' }}
          icon="trash"
          disabled={false}
          className={`${styles.btn} rp-btn warning`}
          onClick={() => {}}
        />
      </div>
    );
  };

  const roleDropdownColumnTemplate = rowData => {
    const rolesList = [{ roleLabel: 'Read only', role: 'read' }, { roleLabel: 'Read/Write', role: 'read_write' }];

    const getActualRole = () => {
      if (rowData) {
        if (rowData.role === 'read_write') {
          return { roleLabel: 'Read/Write', role: 'read_write' };
        } else if (rowData.role === 'read') {
          return { roleLabel: 'Read only', role: 'read' };
        } else {
          return { roleLabel: '', role: '' };
        }
      }
    };
    let actualRole = getActualRole();

    return (
      <div>
        <Dropdown
          optionLabel="roleLabel"
          value={actualRole}
          options={rolesList}
          placeholder={resources.messages.selectContributorRole}
          onChange={e => {
            actualRole = { roleLabel: e.value.roleLabel, role: e.value.role };
          }}
        />
      </div>
    );
  };

  return (
    <div>
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
    </div>
  );
}
