import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './UniqueConstraints.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { UniqueConstraintsService } from 'core/services/UniqueConstraints';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { constraintsReducer } from './_functions/Reducers/constraintsReducer';

import { UniqueConstraintsUtils } from './_functions/Utils/UniqueConstraintsUtils';

export const UniqueConstraints = () => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [constraintsState, constraintsDispatch] = useReducer(constraintsReducer, {
    constraints: [],
    data: {},
    filteredData: []
  });

  useEffect(() => {
    onLoadConstraints();
  }, []);

  const onLoadConstraints = async () => {
    try {
      const response = await UniqueConstraintsService.all();
      constraintsDispatch({ type: 'INITIAL_LOAD', payload: { data: response, constraints: response.list } });
    } catch (error) {
      console.log('error', error);
    }
  };

  const onLoadFilteredData = data => constraintsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const renderColumns = constraints => {
    return Object.keys(constraints[0])
      .filter(item => !item.includes('Id'))
      .map(field => (
        <Column columnResizeMode="expand" field={field} header={field.constraintsName} key={field} sortable={true} />
      ));
  };

  if (constraintsState.isLoading) return <Spinner />;

  return (
    <Fragment>
      <Filters
        data={constraintsState.constraints}
        getFiltredData={onLoadFilteredData}
        inputOptions={['description', 'constraintsName']}
        selectOptions={['fieldNames', 'tableName']}
      />

      {!isEmpty(constraintsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          paginator={true}
          paginatorRight={constraintsState.filteredData.length}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={constraintsState.filteredData.length}
          value={constraintsState.filteredData}>
          {renderColumns(constraintsState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>No with selected parameters</div>
      )}
    </Fragment>
  );
};
