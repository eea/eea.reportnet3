import React, { useContext, useState } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import styles from './Codelist.module.css';

const Codelist = ({ codelist, isDataCustodian = true }) => {
  const [items, setItems] = useState(codelist.items);
  const [isEditing, setIsEditing] = useState(false);
  const [isEditorVisible, setIsEditorVisible] = useState(false);

  const resources = useContext(ResourcesContext);

  const onAddClick = () => {};

  const renderFooter = () => {
    return (
      <div className="p-clearfix" style={{ width: '100%' }}>
        <Button style={{ float: 'left' }} label={resources.messages['add']} icon="add" onClick={() => onAddClick()} />
      </div>
    );
  };

  const renderTable = () => {
    return (
      <DataTable
        autoLayout={true}
        className={styles.itemTable}
        editMode="row"
        footer={isDataCustodian ? renderFooter() : null}
        onRowEditInit={() => {}}
        value={items}>
        <Column rowEditor={true} style={{ width: '70px', textAlign: 'center' }}></Column>
        <Column field="code" header="Code" sortable={true} />
        <Column field="label" header="Label" sortable={true} />
        <Column field="definition" header="Definition" sortable={true} />
      </DataTable>
    );
  };

  return (
    <React.Fragment>
      <TreeViewExpandableItem
        className={styles.codelistItem}
        expanded={false}
        items={[codelist.name, codelist.version, codelist.status, codelist.description]}>
        {renderTable()}
      </TreeViewExpandableItem>
    </React.Fragment>
  );
};

export { Codelist };
