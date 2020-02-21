import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.css';

import { config } from 'conf';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataViewer } from 'ui/views/_components/DataViewer';
import { DataTable } from 'ui/views/_components/DataTable';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TabsValidations = ({
  activeIndex = 0,
  buttonsList = undefined,
  datasetSchemaId,
  hasWritePermissions,
  onTabChange
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(true);
  const [validations, setValidations] = useState([]);
  const [validationColumns, setValidationColumns] = useState([]);
  const [rules, setRules] = useState([]);

  useEffect(() => {
    onLoadValidationsList();
  }, []);
  // }, [isValidationDeleted]);

  // const parseValidationsToDataTable = validations => {
  //   const validationsArray = validations.rules.map(rule => {
  //     console.log({ rule });
  //     return {
  //       ruleName: 'rule.name',
  //       description: 'rule.description',
  //       levelError: ' rule.levelError',
  //       isEnabled: 'rule.isEnabled',
  //       isAutomatic: 'rule.isAutomatic',
  //       actionButtons: ''
  //     };
  //   });
  //   console.log({ validationsArray });
  //   return validationsArray;
  // };

  const onLoadValidationsList = async () => {
    try {
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      setValidations(validationsList);
      setValidationColumns(validationsList);
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_VALIDATIONS',
        content: {
          datasetSchemaId
        }
      });
    } finally {
      setIsLoading(false);
    }
  };

  const validationList = () => {
    console.log({ validations });
    if (isEmpty(validations)) {
      return;
    }
    const headers = [
      {
        id: 'name',
        header: 'Name'
      },
      {
        id: 'shortCode',
        header: 'Short code'
      },
      {
        id: 'ruleDescription',
        header: 'Description'
      },
      {
        id: 'levelError',
        header: 'Level error'
      },
      {
        id: 'enabled',
        header: 'Enabled'
      },
      {
        id: 'automatic',
        header: 'Automatic'
      },
      {
        id: 'rulesActionButtons',
        header: 'Actions'
      }
    ];
    let columnsArray = headers.map(col => <Column sortable={false} key={col.id} field={col.id} header={col.header} />);
    let columns = columnsArray;

    return validations.entityTypes.map(entityType => {
      const validationsFilteredByEntityType = validations.rules.filter(rule => rule.entityType === entityType);
      const totalRecordsBy = 'Total validations';
      return (
        <TabPanel header={entityType} key={entityType} rightIcon={null}>
          <div className={null}>
            <DataTable
              autoLayout={true}
              className={null}
              loading={false}
              paginator={true}
              paginatorRight={validationsFilteredByEntityType.length}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={validationsFilteredByEntityType.length}
              value={validationsFilteredByEntityType}>
              {columns}
            </DataTable>
          </div>
        </TabPanel>
      );
    });
  };

  if (isLoading) {
    return <Spinner />;
  }

  return (
    <TabView activeIndex={activeIndex} onTabChange={onTabChange} renderActiveOnly={false}>
      {validationList()}
    </TabView>
  );
};

export { TabsValidations };
