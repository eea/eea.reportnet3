import React, { Fragment, useCallback, useContext, useEffect, useReducer, useRef, useState } from 'react';

import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

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

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableManagement = () => {
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [PaMNumberToDelete, setPaMNumberToDelete] = useState(null);
  const [tableToCreate, setTableToCreate] = useState(null);

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    console.log('tableToCreate', tableToCreate);
  }, [tableToCreate]);

  const data = [
    {
      PaMNumber: 1,
      nameOfPolicy: 'test',
      singleOrGroup: 'single',
      table1: 'aaaa',
      table2: 'bbbb',
      table3: 'cccc'
    },
    {
      PaMNumber: 2,
      nameOfPolicy: 'test',
      singleOrGroup: 'group',
      table1: 'dddd',
      table2: 'eeee',
      table3: 'ffff'
    }
  ];

  const deletePamBodyTemplate = rowData => {
    return (
      <React.Fragment>
        <span>{rowData.PaMNumber}</span>
        <Button
          icon={'trash'}
          className="p-button-danger"
          onClick={() => {
            setPaMNumberToDelete(rowData.PaMNumber);
            setIsConfirmDeleteVisible(true);
          }}
        />
      </React.Fragment>
    );
  };

  const addTable1 = rowData => {
    return (
      <React.Fragment>
        <Button
          label={'Create Table'}
          icon={'add'}
          onClick={() => {
            setTableToCreate(rowData.table1);
          }}
        />
      </React.Fragment>
    );
  };

  const addTable2 = rowData => {
    return (
      <React.Fragment>
        <Button
          label={'Create Table'}
          icon={'add'}
          onClick={() => {
            setTableToCreate(rowData.table2);
          }}
        />
      </React.Fragment>
    );
  };

  const addTable3 = rowData => {
    return (
      <React.Fragment>
        <Button
          label={'Create Table'}
          icon={'add'}
          onClick={() => {
            setTableToCreate(rowData.table3);
          }}
        />
      </React.Fragment>
    );
  };

  const confirmDelete = PaMNumberToDelete => {
    console.log('PaMNumberToDelete', PaMNumberToDelete);
  };

  const tableColumns = [
    { body: deletePamBodyTemplate, field: 'PaMNumber', header: 'PaMnumber' },
    { field: 'nameOfPolicy', header: 'Name of policy or measure' },
    { field: 'singleOrGroup', header: 'PaM or group of PaMs' },
    {
      body: addTable1,
      field: 'table1',
      header:
        'Table 1: Sectors and gases for reporting on policies and measures and groups of measures, and type of policy instrument'
    },
    {
      body: addTable2,
      field: 'table2',
      header:
        'Table 2: Available results of ex-ante and ex-post assessments of the effects of individual or groups of policies and measures on mitigation of climate change'
    },
    {
      body: addTable3,
      field: 'table3',
      header:
        'Table 3: Available projected and realised costs and benefits of individual or groups of policies and measures on mitigation of climate change'
    }
  ];

  const renderTableColumns = tableColumns.map(col => {
    return isNil(col.body) ? (
      <Column key={col.field} field={col.field} header={col.header} />
    ) : (
      <Column key={col.field} field={col.field} header={col.header} body={col.body} />
    );
  });
  return (
    <Fragment>
      <DataTable
        autoLayout={true}
        // totalRecords={constraintsState.filteredData.length}
        // value={constraintsState.filteredData}
        value={data}>
        {data.map(datos => {
          Object.keys(datos).map(d => {
            return (
              <Column
                // header={messages[selected]['hours']}
                // headerStyle={{ background: online ? '#ff3800' : '', color: online ? 'white' : '' }}
                field={d}
              />
            );
          });
        })}
      </DataTable>

      <DataTable autoLayout={true} value={data}>
        {renderTableColumns}
      </DataTable>

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
