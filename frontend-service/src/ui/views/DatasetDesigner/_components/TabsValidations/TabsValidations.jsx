import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { capitalize, isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.scss';

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

const TabsValidations = withRouter(({ datasetSchemaId, onShowDeleteDialog, setValidationId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(false);
  const [validationsList, setValidationsList] = useState();

  useEffect(() => {
    if (isUndefined(validationsList)) {
      onLoadValidationsList(datasetSchemaId);
    }
  }, []);

  const onLoadValidationsList = async datasetSchemaId => {
    setIsLoading(true);
    try {
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      setValidationsList(validationsList);
    } catch (error) {
      console.log(error);
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

  const parseToValidationsView = validations => {
    let validationsView = validations;
    validationsView.forEach(validationDTO => {
      validationDTO.actionButtons = (
        <div className={styles.actionButtons}>
          <Button
            className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
            icon="trash"
            onClick={() => {
              setValidationId(validationDTO.id);
              onShowDeleteDialog();
            }}
            type="button"
          />
        </div>
      );

      if (validationDTO.automatic) {
        validationDTO.automatic = (
          <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      } else {
        // validationDTO.actionButtons = (
        //   <div>
        //     <Button type="button" icon="edit" className={`p-button-rounded p-button-secondary`} />
        //     <Button type="button" icon="trash" className={`p-button-rounded p-button-secondary`} />
        //   </div>
        // );
        validationDTO.automatic = (
          <FontAwesomeIcon icon={AwesomeIcons('cross')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      }
      if (validationDTO.enabled) {
        validationDTO.enabled = (
          <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      } else {
        validationDTO.enabled = (
          <FontAwesomeIcon icon={AwesomeIcons('cross')} style={{ float: 'center', color: 'var(--black)' }} />
        );
      }
    });
    return validationsView;
  };

  const ValidationList = () => {
    if (isUndefined(validationsList) || isEmpty(validationsList)) {
      return (
        <div>
          <h3>{resources.messages['emptyValidations']}</h3>
        </div>
      );
    }
    console.log({ validationsList });
    const headers = getValidationHeaders();
    let columnsArray = headers.map(col => <Column sortable={false} key={col.id} field={col.id} header={col.header} />);
    let columns = columnsArray;

    return validationsList.entityTypes.map(entityType => {
      const validationsFilteredByEntityType = validationsList.validations.filter(
        validation => validation.entityType === entityType
      );
      const validationsView = parseToValidationsView(validationsFilteredByEntityType);

      const paginatorRightText = `${capitalize(entityType)} records: ${validationsFilteredByEntityType.length}`;
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
