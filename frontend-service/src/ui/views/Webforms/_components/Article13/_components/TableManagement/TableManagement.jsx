import React, { Fragment, useCallback, useContext, useEffect, useReducer, useRef, useState } from 'react';

import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import styles from './TableManagement.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const TableManagement = ({ dataflowId, datasetId, records }) => {
  const resources = useContext(ResourcesContext);

  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [PaMNumberToDelete, setPaMNumberToDelete] = useState(null);
  const [tableToCreate, setTableToCreate] = useState(null);
  const [tableRecords, setTableRecords] = useState([]);

  useEffect(() => {
    parsePamsRecords();
  }, [records]);

  const parsePamsRecords = () => {
    setTableRecords(
      records.map(record => {
        let data = {};

        record.elements.forEach(element => (data = { ...data, [element.name]: element.value }));

        return data;
      })
    );
  };

  const addTable1 = rowData => (
    <Button label={'Create Table'} icon={'add'} onClick={() => setTableToCreate(rowData.table1)} />
  );

  const addTable2 = rowData => (
    <Button label={'Create Table'} icon={'add'} onClick={() => setTableToCreate(rowData.table2)} />
  );

  const addTable3 = rowData => (
    <Button label={'Create Table'} icon={'add'} onClick={() => setTableToCreate(rowData.table3)} />
  );

  const confirmDelete = PaMNumberToDelete => {
    // console.log('PaMNumberToDelete', PaMNumberToDelete);
  };

  const addTableTemplate = rowData => {
    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <Button
          className={'p-button-secondary'}
          icon={'add'}
          label={'Create Table'}
          onClick={() => setTableToCreate(rowData.table3)}
        />
      </div>
    );
  };

  const tableColumns = [
    { field: 'Id', header: 'PaMnumber' },
    { field: 'Title', header: 'Name of policy or measure' },
    { field: 'IsGroup', header: 'PaM or group of PaMs' },
    {
      body: addTableTemplate,
      field: 'table1',
      header:
        'Table 1: Sectors and gases for reporting on policies and measures and groups of measures, and type of policy instrument'
    },
    {
      body: addTableTemplate,
      field: 'table2',
      header:
        'Table 2: Available results of ex-ante and ex-post assessments of the effects of individual or groups of policies and measures on mitigation of climate change'
    },
    {
      body: addTableTemplate,
      field: 'table3',
      header:
        'Table 3: Available projected and realised costs and benefits of individual or groups of policies and measures on mitigation of climate change'
    }
  ];

  const renderActionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      header={resources.messages['actions']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const actionsTemplate = () => (
    <ActionsColumn
      onDeleteClick={() => setIsConfirmDeleteVisible(true)}
      // onEditClick={() => manageDialogs('isManageUniqueConstraintDialogVisible', true)}
      onEditClick={() => {}}
    />
  );

  const renderTableColumns = () => {
    const data = tableColumns.map(col => {
      return <Column key={col.field} field={col.field} header={col.header} body={col.body} />;
    });

    data.push(renderActionButtonsColumn);

    return data;
  };

  console.log('tableRecords', tableRecords);

  return (
    <Fragment>
      <DataTable autoLayout={true} value={tableRecords}>
        {renderTableColumns()}
      </DataTable>

      <div className={styles.addButtons}>
        <Button label={'Add Single'} icon={'add'} />
        <Button label={'Add Group'} icon={'add'} />
      </div>

      {isConfirmDeleteVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteTabHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => {
            confirmDelete(PaMNumberToDelete);
            setIsConfirmDeleteVisible(false);
          }}
          onHide={() => setIsConfirmDeleteVisible(false)}
          visible={isConfirmDeleteVisible}>
          ARE YOU SURE YOU WANT TO DELETE
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
