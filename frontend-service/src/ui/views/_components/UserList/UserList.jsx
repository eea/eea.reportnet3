import React, { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './UserList.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const UserList = ({ dataflowId, representativeId }) => {
  const resources = useContext(ResourcesContext);

  const [userListData, setUserListData] = useState([]);
  const [filteredData, setFilteredData] = useState(userListData);
  const [isDataFiltered, setIsDataFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (!isDataFiltered) {
      setFilteredData(userListData);
    }
  }, [isDataFiltered]);

  const fetchData = async () => {
    try {
      let users;
      setIsLoading(true);
      if (isNil(representativeId)) {
        users = await DataflowService.getAllDataflowsUserList();
      } else {
        users = await DataflowService.getUserList(dataflowId, representativeId);
      }

      setUserListData(users);
      setFilteredData(users);
    } catch (error) {
      console.log('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getFilteredState = value => setIsDataFiltered(value);

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered && userListData.length !== filteredData.length
        ? `${resources.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {userListData.length} {resources.messages['records'].toLowerCase()}
      {isDataFiltered && userListData.length === filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const onLoadFilteredData = value => setFilteredData(value);

  const renderColumns = users => {
    const fieldColumns = Object.keys(users[0])
      .filter(key => key.includes('dataflowName'))
      .map(field => <Column field={field} header={resources.messages['dataflowName']} key={field} sortable={true} />);
    return fieldColumns;
  };

  return (
    <div className={styles.container}>
      {isLoading ? (
        <Spinner />
      ) : isEmpty(userListData) ? (
        <div className={styles.noUsers}>{resources.messages['noUsers']}</div>
      ) : (
        <div className={styles.users}>
          {isNil(representativeId) ? (
            <Filters
              data={userListData}
              getFilteredData={onLoadFilteredData}
              getFilteredSearched={getFilteredState}
              inputOptions={['dataflowName', 'email']}
              selectOptions={['role']}
            />
          ) : (
            <Filters
              data={userListData}
              getFilteredData={onLoadFilteredData}
              getFilteredSearched={getFilteredState}
              inputOptions={['email']}
              selectOptions={['role']}
            />
          )}

          {!isEmpty(filteredData) ? (
            <DataTable
              value={filteredData}
              paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
              paginator={true}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={userListData.length}>
              {isNil(representativeId) && (
                <Column field="dataflowName" header={resources.messages['dataflowName']} sortable={true} />
              )}
              <Column field="email" header={resources.messages['user']} sortable={true} />
              <Column field="role" header={resources.messages['role']} sortable={true} />
            </DataTable>
          ) : (
            <div className={styles.emptyFilteredData}>{resources.messages['noUsersWithSelectedParameters']}</div>
          )}
        </div>
      )}
    </div>
  );
};
