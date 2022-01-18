import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './UserList.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { useFilters } from 'views/_functions/Hooks/useFilters';

export const UserList = ({ dataflowId, dataflowType, representativeId }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [userListData, setUserListData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const { filteredData, isFiltered } = useFilters('userList');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      let userData;
      setIsLoading(true);
      if (isNil(representativeId) && isNil(dataflowId)) {
        userData = await DataflowService.getAllDataflowsUserList();
      } else if (isNil(representativeId) && !isNil(dataflowId)) {
        userData = await DataflowService.getRepresentativesUsersList(dataflowId);
      } else {
        userData = await DataflowService.getUserList(dataflowId, representativeId);
      }
      setUserListData(userData);
    } catch (error) {
      console.error('UserList - fetchData.', error);
      notificationContext.add({ type: 'LOAD_USERS_LIST_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getFilters = filterOptions => {
    return <MyFilters className="userList" data={userListData} options={filterOptions} viewType="userList" />;
  };

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isFiltered && userListData.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {userListData.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isFiltered && userListData.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const getUserListColumns = () => {
    const columns = [
      { key: 'role', header: resourcesContext.messages['role'] },
      { key: 'email', header: resourcesContext.messages['user'] }
    ];
    if (isNil(representativeId) && isNil(dataflowId)) {
      columns.splice(0, 0, { key: 'dataflowName', header: resourcesContext.messages['dataflowName'] });
    }
    if (isNil(representativeId) && !isNil(dataflowId)) {
      columns.push({
        key: 'dataProviderName',
        header: TextByDataflowTypeUtils.getLabelByDataflowType(
          resourcesContext.messages,
          dataflowType,
          'userListDataProviderColumnHeader'
        )
      });
    }

    return columns.map(column => <Column field={column.key} header={column.header} key={column.key} sortable />);
  };

  const filterOptionsWithDataflowIdRepresentativeId = [
    {
      type: 'MULTI_SELECT',
      key: 'role',
      label: resourcesContext.messages['role']
    },
    { type: 'INPUT', key: 'email', label: resourcesContext.messages['email'] },
    {
      type: 'MULTI_SELECT',
      key: 'dataProviderName',
      showInput: true,
      label: TextByDataflowTypeUtils.getLabelByDataflowType(
        resourcesContext.messages,
        dataflowType,
        'userListDataProviderFilterLabel'
      )
    }
  ];

  const filterOptionsNoRepresentative = [
    {
      type: 'INPUT',
      key: 'dataflowName',
      label: resourcesContext.messages['dataflowName']
    },
    { type: 'MULTI_SELECT', nestedOptions: [{ key: 'role', label: resourcesContext.messages['role'] }] },
    {
      type: 'INPUT',
      key: 'email',
      label: resourcesContext.messages['email']
    }
  ];

  const filterOptionsHasRepresentativeId = [
    { type: 'MULTI_SELECT', nestedOptions: [{ key: 'role', label: resourcesContext.messages['role'] }] },
    { type: 'INPUT', key: 'email', label: resourcesContext.messages['email'] }
  ];

  const renderFilters = () => {
    if (isNil(representativeId) && isNil(dataflowId)) {
      return getFilters(filterOptionsNoRepresentative);
    } else if (isNil(representativeId) && !isNil(dataflowId)) {
      return getFilters(filterOptionsWithDataflowIdRepresentativeId);
    } else {
      return getFilters(filterOptionsHasRepresentativeId);
    }
  };

  const renderUsersListContent = () => {
    if (isLoading) {
      return <Spinner />;
    }

    if (isEmpty(userListData)) {
      return <div className={styles.noUsers}>{resourcesContext.messages['noUsers']}</div>;
    }

    return (
      <div className={styles.users}>
        {renderFilters()}
        {renderUsersListTable()}
      </div>
    );
  };

  const renderUsersListTable = () => {
    if (isEmpty(filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>{resourcesContext.messages['noUsersWithSelectedParameters']}</div>
      );
    } else {
      return (
        <DataTable
          paginator={true}
          paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary="usersList"
          totalRecords={userListData.length}
          value={filteredData}>
          {getUserListColumns()}
        </DataTable>
      );
    }
  };

  return <div className={styles.container}>{renderUsersListContent()} </div>;
};
