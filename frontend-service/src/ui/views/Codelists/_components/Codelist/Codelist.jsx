import React, { useState } from 'react';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import styles from './Codelist.module.css';

const Codelist = ({ codelist }) => {
  const [isEditorVisible, setIsEditorVisible] = useState(false);

  return (
    <React.Fragment>
      <li className={styles.codelistItem} onClick={() => setIsEditorVisible(!isEditorVisible)}>
        <span>{codelist.name}</span>
        <span>{codelist.version}</span>
        <span>{codelist.status}</span>
        <span>{codelist.description}</span>{' '}
      </li>
      {isEditorVisible ? (
        <DataTable className={styles.itemTable} value={codelist.items} autoLayout={true}>
          <Column field="code" header="Code" sortable={true} />
          <Column field="label" header="Label" sortable={true} />
          <Column field="definition" header="Definition" sortable={true} />
        </DataTable>
      ) : null}
    </React.Fragment>
  );
};

export { Codelist };
