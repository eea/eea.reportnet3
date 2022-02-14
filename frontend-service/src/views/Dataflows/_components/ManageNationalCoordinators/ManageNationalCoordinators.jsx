import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ManageNationalCoordinators.module.scss';

import { AddNationalCoordinator } from './_components/AddNationalCoordinator';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { UserRightService } from 'services/UserRightService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { CountryUtils } from 'views/_functions/Utils/CountryUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

import { useFilters } from 'views/_functions/Hooks/useFilters';

export const ManageNationalCoordinators = ({ onCloseDialog, isDialogVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [nationalCoordinatorsData, setNationalCoordinatorsData] = useState([]);

  const [isLoading, setIsLoading] = useState(false);

  //const { filteredData, isFiltered } = useFilters('manageNationalCoordinators');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const { data } = await UserRightService.getNationalCoordinators();
      setNationalCoordinatorsData(parseNationalCoordinatorsList(data));
    } catch (error) {
      console.error('NationalCoordinators - fetchData.', error);
      notificationContext.add({ type: 'LOAD_USERS_LIST_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const parseNationalCoordinatorsList = nationalCoordinatorsList => {
    const nationalCoordinators = [];

    if (!isEmpty(nationalCoordinatorsList)) {
      nationalCoordinatorsList.forEach(nationalCoordinator => {
        const countryName = CountryUtils.getCountryName(nationalCoordinator.countryCode);
        nationalCoordinator.countryName = countryName;
        nationalCoordinators.push(nationalCoordinator);
      });
    }

    return nationalCoordinators;
  };

  const getActionsTemplate = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteButton}`}
        icon="trash"
        //onClick={() => onShowDeleteDialog(webformRow)}
      />
    );
  };

  const renderTableColumns = () => {
    const columns = [
      {
        key: 'email',
        header: resourcesContext.messages['manageNationalCoordinatorsDialogColumn']
      },
      {
        key: 'countryName',
        header: resourcesContext.messages['manageRolesDialogDataProviderColumn']
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable
      />
    ));
  };

  const filterOptions = [
    {
      key: 'search',
      label: resourcesContext.messages['search'],
      searchBy: ['nationalCoordinators'],
      type: 'SEARCH'
    },
    {
      key: 'countries',
      label: resourcesContext.messages['countries'],
      dropdownOptions: ['country1, country2, country3'],
      type: 'DROPDOWN'
    }
  ];

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isEmpty(nationalCoordinatorsData)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <div className={styles.table}>
        <Filters
          className="manageNationalCoordinators"
          // onFilter={onLoadReportingObligations}
          // onReset={onLoadReportingObligations}
          options={filterOptions}
        />

        <DataTable
          autoLayout
          className={styles.dialogContent}
          hasDefaultCurrentPage
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={nationalCoordinatorsData.length}
          value={nationalCoordinatorsData}>
          {renderTableColumns()}
        </DataTable>
      </div>
    );
  };

  const dialogFooter = (
    <div className={styles.buttonsDialogFooter}>
      <AddNationalCoordinator />

      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  return (
    <Dialog
      blockScroll={false}
      footer={dialogFooter}
      header={resourcesContext.messages['manageNationalCoordinators']}
      modal
      onHide={onCloseDialog}
      style={{ width: '80%', maxWidth: '650px' }}
      visible={isDialogVisible}>
      {renderDialogContent()}
    </Dialog>
  );
};
