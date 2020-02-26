import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { capitalize, isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TabsValidations = withRouter(({ datasetSchemaId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const [isLoading, setIsLoading] = useState(false);
  const [validations, setValidations] = useState();

  useEffect(() => {
    if (isUndefined(validations)) {
      onLoadValidationsList(datasetSchemaId);
    }
  }, []);

  const onLoadValidationsList = async datasetSchemaId => {
    setIsLoading(true);
    try {
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      console.log('View', validationsList);
      setValidations(validationsList);
    } catch (error) {
      console.log(validations);
      // notificationContext.add({
      //   type: 'VALIDATION_SERVICE_GET_ALL_ERROR'
      // });
    } finally {
      setIsLoading(false);
    }
  };

  const getValidationHeaders = () => {
    return [
      {
        id: 'shortCode',
        header: resources.messages['ruleShortCode']
      },
      {
        id: 'name',
        header: resources.messages['ruleName']
      },
      {
        id: 'description',
        header: resources.messages['ruleDescription']
      },
      {
        id: 'levelError',
        header: resources.messages['ruleLevelError']
      },
      {
        id: 'enabled',
        header: resources.messages['ruleEnabled']
      },
      {
        id: 'automatic',
        header: resources.messages['ruleAutomatic']
      },
      {
        id: 'actionButtons',
        header: resources.messages['ruleActions']
      }
    ];
  };

  const parseToValidationsView = rules => {
    console.log({ validations });
    let validationsView = rules;
    console.log({ validationsView });
    validationsView.forEach(ruleDTO => {
      ruleDTO.actionButtons = (
        <div>
          <Button
            type="button"
            icon="trash"
            className={`p-button-secondary`}
            onClick={() => {
              //Parrastia's time
            }}
          />
        </div>
      );

      if (ruleDTO.automatic) {
        ruleDTO.automatic = (
          <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      } else {
        // ruleDTO.actionButtons = (
        //   <div>
        //     <Button type="button" icon="edit" className={`p-button-rounded p-button-secondary`} />
        //     <Button type="button" icon="trash" className={`p-button-rounded p-button-secondary`} />
        //   </div>
        // );
        ruleDTO.automatic = (
          <FontAwesomeIcon icon={AwesomeIcons('cross')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      }
      if (ruleDTO.enabled) {
        ruleDTO.enabled = (
          <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      } else {
        ruleDTO.enabled = (
          <FontAwesomeIcon icon={AwesomeIcons('cross')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      }
    });
    return validationsView;
  };

  const ValidationList = () => {
    if (isUndefined(validations) || isEmpty(validations)) {
      return (
        <div>
          <h3>{resources.messages['emptyValidations']}</h3>
        </div>
      );
    }

    const headers = getValidationHeaders();
    let columnsArray = headers.map(col => <Column sortable={false} key={col.id} field={col.id} header={col.header} />);
    let columns = columnsArray;

    return validations.entityTypes.map(entityType => {
      const validationsFilteredByEntityType = validations.rules.filter(rule => rule.entityType === entityType);
      const validationsView = parseToValidationsView(validationsFilteredByEntityType);

      const paginatorRightText = `${capitalize(entityType)} records: ${validationsFilteredByEntityType.length}`;
      console.log({ validationsView });
      return (
        <div className={null}>
          <DataTable
            autoLayout={true}
            className={null}
            loading={false}
            paginator={true}
            paginatorRight={paginatorRightText}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={validationsView.length}
            value={validationsView}>
            {columns}
          </DataTable>
        </div>
        // <TabPanel header={entityType} key={entityType} rightIcon={null}>
        //   <div className={null}>
        //     <DataTable
        //       autoLayout={true}
        //       className={null}
        //       loading={false}
        //       paginator={true}
        //       paginatorRight={paginatorRightText}
        //       rows={10}
        //       rowsPerPageOptions={[5, 10, 15]}
        //       totalRecords={validationsFilteredByEntityType.length}
        //       value={validationsFilteredByEntityType}>
        //       {columns}
        //     </DataTable>
        //   </div>
        // </TabPanel>
      );
    });
  };

  if (isLoading) {
    return <Spinner />;
  }

  return <ValidationList />;
});

export { TabsValidations };
