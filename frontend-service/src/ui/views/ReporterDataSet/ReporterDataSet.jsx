/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';

import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './ReporterDataSet.module.css';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from './_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Growl } from 'primereact/growl';
import { InputSwitch } from 'primereact/inputswitch';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ReporterDatasetContext } from './_components/_context/ReporterDataSetContext';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Snapshots } from './_components/Snapshots/index';

import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from './_components/TabsSchema';
import { Title } from './_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { ValidationViewer } from './_components/ValidationViewer';
import { WebFormData } from './_components/WebFormData/WebFormData';

import { DatasetService } from 'core/services/DataSet';

import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const ReporterDataset = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [activeIndex, setActiveIndex] = useState();
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [datasetTitle, setDatasetTitle] = useState('');
  const [datasetHasErrors, setDatasetHasErrors] = useState(false);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [exportDatasetData, setExportDatasetData] = useState(undefined);
  const [exportDatasetDataName, setExportDatasetDataName] = useState('');
  const [isDataDeleted, setIsDataDeleted] = useState(false);
  const [isInputSwitchChecked, setIsInputSwitchChecked] = useState(false);
  const [isWebFormMMR, setIsWebFormMMR] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingFile, setLoadingFile] = useState(false);
  const [recordPositionId, setRecordPositionId] = useState(-1);
  const [selectedRecordErrorId, setSelectedRecordErrorId] = useState(-1);
  const [isSnapshotsBarVisible, setIsSnapshotsBarVisible] = useState(false);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [tableSchemaNames, setTableSchemaNames] = useState([]);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationsVisible, setValidationsVisible] = useState(false);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [tableSchemaId, setTableSchemaId] = useState();

  let exportMenuRef = useRef();

  let growlRef = useRef();

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      { label: resources.messages['dataset'] }
    ]);
  }, []);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    onLoadDatasetSchema();
  }, [isDataDeleted]);

  useEffect(() => {
    let exportOptions = config.exportTypes;
    const exportOptionsFilter = exportOptions.filter(type => type.code !== 'csv');

    setExportButtonsList(
      exportOptionsFilter.map(type => ({
        label: type.text,
        icon: config.icons['archive'],
        command: () => onExportData(type.code)
      }))
    );
  }, [datasetTitle]);

  useEffect(() => {
    if (!isUndefined(exportDatasetData)) {
      DownloadFile(exportDatasetData, exportDatasetDataName);
    }
  }, [exportDatasetData]);

  const createFileName = (fileName, fileType) => {
    return `${fileName}.${fileType}`;
  };

  const checkIsWebFormMMR = datasetName => {
    const mmrDatasetName = 'MMR_TEST';
    if (datasetName.toString().toLowerCase() === mmrDatasetName.toString().toLowerCase()) {
      setIsInputSwitchChecked(true);
      setIsWebFormMMR(true);
    } else {
      setIsWebFormMMR(false);
    }
  };

  const getPosition = button => {
    const buttonTopPosition = button.top;
    const buttonLeftPosition = button.left;

    const exportDatasetMenu = document.getElementById('exportDataSetMenu');
    exportDatasetMenu.style.top = buttonTopPosition;
    exportDatasetMenu.style.left = buttonLeftPosition;
  };

  const onConfirmDelete = async () => {
    setDeleteDialogVisible(false);
    const dataDeleted = await DatasetService.deleteDataById(datasetId);
    if (dataDeleted) {
      setIsDataDeleted(true);
    }
  };

  const onConfirmValidate = async () => {
    setValidateDialogVisible(false);
    await DatasetService.validateDataById(datasetId);
  };

  const onExportData = async fileType => {
    setLoadingFile(true);
    try {
      setExportDatasetDataName(createFileName(datasetTitle, fileType));
      setExportDatasetData(await DatasetService.exportDataById(datasetId, fileType));
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingFile(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(dataflowId);
      const datasetStatistics = await DatasetService.errorStatisticsById(datasetId);
      setTableSchemaId(datasetSchema.tables[0].tableSchemaId);
      setDatasetTitle(datasetStatistics.datasetSchemaName);
      checkIsWebFormMMR(datasetStatistics.datasetSchemaName);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            hasErrors: {
              ...datasetStatistics.tables.filter(table => table['tableSchemaId'] === tableSchema['tableSchemaId'])[0]
            }.hasErrors
          };
        })
      );
      setTableSchemaNames(tableSchemaNamesList);
      setTableSchemaColumns(
        datasetSchema.tables.map(table => {
          return table.records[0].fields.map(field => {
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${field['name'].charAt(0).toUpperCase()}${field['name'].slice(1)}`,
              type: field['type'],
              recordId: field['recordId']
            };
          });
        })
      );

      setDatasetHasErrors(datasetStatistics.datasetErrors);
    } catch (error) {
      const errorResponse = error.response;
      if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    }
    setLoading(false);
  };

  const onSetVisible = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onTabChange = tableSchemaId => {
    setActiveIndex(tableSchemaId.index);
  };

  const showWebFormInputSwitch = () => {
    if (isWebFormMMR) {
      return (
        <div className={styles.InputSwitchContainer}>
          <div className={styles.InputSwitchDiv}>
            <span className={styles.InputSwitchText}>{resources.messages['grid']}</span>
            {WebFormInputSwitch}
            <span className={styles.InputSwitchText}>{resources.messages['webForm']}</span>
          </div>
        </div>
      );
    }
  };

  const isWebForm = () => {
    if (isInputSwitchChecked) {
      return <WebFormData datasetId={datasetId} tableSchemaId={tableSchemaId} />;
    } else {
      return (
        <TabsSchema
          activeIndex={activeIndex}
          onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
          recordPositionId={recordPositionId}
          selectedRecordErrorId={selectedRecordErrorId}
          tables={tableSchema}
          tableSchemaColumns={tableSchemaColumns}
          isWebFormMMR={isWebFormMMR}
          hasWritePermissions={hasWritePermissions}
        />
      );
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <Growl ref={growlRef} />
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  let WebFormInputSwitch = (
    <InputSwitch
      className={styles.WebFormInputSwitch}
      checked={isInputSwitchChecked}
      onChange={e => {
        setIsInputSwitchChecked(e.value);
      }}
    />
  );

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <>
      <Title title={`${resources.messages['titleDataset']}${datasetTitle}`} />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions}
              icon={loadingFile ? 'spinnerAnimate' : 'import'}
              label={resources.messages['export']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
              id="exportDataSetMenu"
              onShow={e => {
                getPosition(e.target.style);
              }}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'trash'}
              label={resources.messages['deleteDatasetData']}
              disabled={!hasWritePermissions || isWebFormMMR}
              onClick={() => onSetVisible(setDeleteDialogVisible, true)}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions || isWebFormMMR}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => onSetVisible(setValidateDialogVisible, true)}
              ownButtonClasses={null}
              iconClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!datasetHasErrors || isWebFormMMR}
              icon={'warning'}
              label={resources.messages['showValidations']}
              onClick={() => onSetVisible(setValidationsVisible, true)}
              ownButtonClasses={null}
              iconClasses={datasetHasErrors ? 'warning' : ''}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={isWebFormMMR}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => onSetVisible(setDashDialogVisible, true)}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={!hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
          </div>
        </Toolbar>
      </div>
      <ReporterDatasetContext.Provider
        value={{
          validationsVisibleHandler: null,
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRecordErrorId(selectedRecordErrorId);
          }
        }}>
        {showWebFormInputSwitch()}
        {isWebForm()}
      </ReporterDatasetContext.Provider>
      <Dialog
        dismissableMask={true}
        header={resources.messages['titleDashboard']}
        onHide={() => onSetVisible(setDashDialogVisible, false)}
        style={{ width: '70vw' }}
        visible={dashDialogVisible}>
        <Dashboard refresh={dashDialogVisible} />
      </Dialog>
      <ReporterDatasetContext.Provider
        value={{
          onValidationsVisible: () => {
            onSetVisible(setValidationsVisible, false);
          },
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setActiveIndex(tableSchemaId);
            setRecordPositionId(posIdRecord);
            setSelectedRecordErrorId(selectedRecordErrorId);
          }
        }}>
        <Dialog
          dismissableMask={true}
          header={resources.messages['titleValidations']}
          maximizable
          onHide={() => onSetVisible(setValidationsVisible, false)}
          style={{ width: '80%' }}
          visible={validationsVisible}
          className={styles.paginatorValidationViewer}>
          <ValidationViewer
            datasetId={datasetId}
            datasetName={datasetTitle}
            visible={validationsVisible}
            hasWritePermissions={hasWritePermissions}
            tableSchemaNames={tableSchemaNames}
          />
        </Dialog>
      </ReporterDatasetContext.Provider>
      <ConfirmDialog
        header={resources.messages['deleteDatasetHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={onConfirmDelete}
        onHide={() => onSetVisible(setDeleteDialogVisible, false)}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDatasetConfirm']}
      </ConfirmDialog>
      <ConfirmDialog
        header={resources.messages['validateDataset']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={onConfirmValidate}
        onHide={() => onSetVisible(setValidateDialogVisible, false)}
        visible={validateDialogVisible}>
        {resources.messages['validateDatasetConfirm']}
      </ConfirmDialog>
      <Snapshots
        datasetId={datasetId}
        dataflowId={dataflowId}
        growlRef={growlRef}
        isSnapshotsBarVisible={isSnapshotsBarVisible}
        setIsSnapshotsBarVisible={setIsSnapshotsBarVisible}
      />
    </>
  );
});
