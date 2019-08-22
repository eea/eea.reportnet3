import React, { useContext, useState, useEffect } from 'react';

import styles from './Contributor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { Dropdown } from 'primereact/dropdown';

export function Contributor({ contributorData }) {
  let initialRoleState = { roleText: '', role: '' };

  if (contributorData.role === 'read') {
    initialRoleState = { roleText: 'Read', role: 'read' };
  } else if (contributorData.role === 'read_write') {
    initialRoleState = { roleText: 'Read/Write', role: 'read_write' };
  }

  const resources = useContext(ResourcesContext);

  const [selectedRole, setSelectedRole] = useState(initialRoleState);

  const roles = [{ roleText: 'Read', role: 'read' }, { roleText: 'Read/Write', role: 'read_write' }];
  //TODO UPDATE ROLE ON SELECTED ROLE
  //TODO IMPLEMENT DELETE BUTTON
  //TODO FINISH EVERYTHING

  return (
    <li className={styles.listItem}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <p>{contributorData.login}</p>
          <div className={styles.listActions}>
            <Dropdown
              optionLabel="roleText"
              value={selectedRole}
              options={roles}
              onChange={e => {
                setSelectedRole({ roleText: e.value.roleText, role: e.value.role });
              }}
              placeholder={resources.messages.selectContributorRole}
            />
            <Button
              tooltip={resources.messages.deleteContributor}
              tooltipOptions={{ position: 'left' }}
              icon="trash"
              disabled={false}
              className={`${styles.btn} rp-btn warning`}
              onClick={() => {}}
            />
          </div>
        </div>
      </div>
    </li>
  );
}
