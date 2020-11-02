import React, { Fragment, useCallback, useContext, useEffect, useReducer, useRef } from 'react';

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
import { DataTable } from 'ui/views/_components/DataTable';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableManagement = () => {
  const data = [
    {
      PaMNumber: 0,
      nameOfPolicy: 'test',
      singleOrGroup: 'single',
      table1: 'aaaa',
      table2: 'bbbb',
      table3: 'cccc'
    },
    {
      PaMNumber: 0,
      nameOfPolicy: 'test',
      singleOrGroup: 'group',
      table1: 'dddd',
      table2: 'eeee',
      table3: 'ffff'
    }
  ];

  return (
    <Fragment>
      <DataTable
        autoLayout={true}
        // totalRecords={constraintsState.filteredData.length}
        // value={constraintsState.filteredData}
        value={data}>
        {data.map(datos => {
          console.log('datos', datos);

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
    </Fragment>
  );
};
