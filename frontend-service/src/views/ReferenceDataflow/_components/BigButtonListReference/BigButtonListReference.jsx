import { Fragment, useContext, useEffect, useLayoutEffect, useReducer } from 'react';
import { useNavigate } from 'react-router-dom';

import isNil from 'lodash/isNil';
import remove from 'lodash/remove';

import { config } from 'conf';
import { DataflowConfig } from 'repositories/config/DataflowConfig';
import { routes } from 'conf/routes';

import styles from './BigButtonListReference.module.scss';

import { BigButton } from 'views/_components/BigButton';
import { Button } from 'views/_components/Button';
import { CloneSchemas } from 'views/Dataflow/_components/CloneSchemas';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { Dialog } from 'views/_components/Dialog';
import { NewDatasetSchemaForm } from 'views/_components/NewDatasetSchemaForm';

import { DataCollectionService } from 'services/DataCollectionService';
import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { referenceBigButtonsReducer } from './_functions/Reducers/referenceBigButtonsReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { MetadataUtils } from 'views/_functions/Utils';

export const BigButtonListReference = ({
  dataflowId,
  dataflowState,
  onSaveName,
  onUpdateData,
  setIsCreatingReferenceDatasets,
  setIsImportingDataflow,
  setUpdatedDatasetSchema
}) => {
  const navigate = useNavigate();

  const { showLoading, hideLoading } = useContext(LoadingContext);
  const [referenceBigButtonsState, referenceBigButtonsDispatch] = useReducer(referenceBigButtonsReducer, {
    cloneDataflow: { id: null, name: '' },
    deleteIndex: null,
    dialogVisibility: {
      isCreateReference: false,
      isDeleteDataset: false,
      isImportSchemaVisible: false,
      isNewDataset: false,
      isTableWithNoPK: false
    },
    hasDatasets: false,
    isCloningDataflow: false,
    isCreateReferenceEnabled: false,
    isDesignStatus: false,
    isImportingDataflow: false
  });

  const { resetFiltersState: resetCloneSchemasFiltersState } = useFilters('cloneSchemas');

  useEffect(() => {
    setIsDesignStatus(dataflowState.status === config.dataflowStatus.DESIGN);
  }, [dataflowState.status]);

  useEffect(() => {
    setHasDatasets(dataflowState.data?.designDatasets?.length);
  }, [dataflowState.data.designDatasets]);

  useLayoutEffect(() => {
    onLoadSchemasValidations();
  }, []);

  useCheckNotifications(
    ['COPY_DATASET_SCHEMA_COMPLETED_EVENT', 'COPY_DATASET_SCHEMA_FAILED_EVENT', 'COPY_DATASET_SCHEMA_NOT_FOUND_EVENT'],
    setCloneLoading,
    false
  );

  useCheckNotifications(
    ['IMPORT_DATASET_SCHEMA_COMPLETED_EVENT', 'IMPORT_DATASET_SCHEMA_FAILED_EVENT'],
    setImportingLoading,
    false
  );

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const {
    cloneDataflow,
    deleteIndex,
    dialogVisibility,
    isCloningDataflow,
    isCreateReferenceEnabled,
    isDesignStatus,
    isImportingDataflow,
    hasDatasets
  } = referenceBigButtonsState;

  useEffect(() => {
    const response = notificationContext.hidden.find(
      notification => notification.key === 'NO_PK_REFERENCE_DATAFLOW_ERROR_EVENT'
    );
    if (response) {
      setIsCreatingReferenceDatasets(false);
      handleDialogs({ dialog: 'isTableWithNoPK', isVisible: true });
    }
  }, [notificationContext]);

  function setIsDesignStatus(isDesignStatus) {
    referenceBigButtonsDispatch({ type: 'SET_IS_DESIGN_STATUS', payload: { isDesignStatus } });
  }

  function setHasDatasets(hasDatasets) {
    referenceBigButtonsDispatch({ type: 'SET_HAS_DATASETS', payload: { hasDatasets } });
  }

  const handleDialogs = ({ dialog, isVisible }) => {
    referenceBigButtonsDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
  };

  function setImportingLoading(value) {
    referenceBigButtonsDispatch({ type: 'IS_IMPORTING_DATAFLOW', payload: { status: value } });
  }

  function setCloneLoading(value) {
    referenceBigButtonsDispatch({ type: 'IS_CLONING_DATAFLOW', payload: { status: value } });
  }

  const cloneDatasetSchemas = async () => {
    handleDialogs({ dialog: 'cloneDialogVisible', isVisible: false });
    setCloneLoading(true);

    notificationContext.add(
      {
        type: 'CLONE_DATASET_SCHEMAS_INIT',
        content: { customContent: { sourceDataflowName: cloneDataflow.name, targetDataflowName: dataflowState.name } }
      },
      true
    );

    try {
      await DataflowService.cloneSchemas(cloneDataflow.id, dataflowId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('BigButtonListReference - cloneDatasetSchemas.', error);
        notificationContext.add({ type: 'CLONE_NEW_SCHEMA_ERROR' }, true);
      }
    }
  };

  const getCloneDataflow = dataflow =>
    referenceBigButtonsDispatch({ type: 'GET_DATAFLOW_TO_CLONE', payload: { dataflow } });

  const onImportSchema = isVisible => {
    handleDialogs({ dialog: 'isImportSchemaVisible', isVisible });
  };

  const onImportSchemaError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
    } else {
      notificationContext.add({ type: 'IMPORT_DATASET_SCHEMA_FAILED_EVENT' }, true);
    }
  };

  const onToggleNewDatasetDialog = isVisible => handleDialogs({ dialog: 'isNewDataset', isVisible });

  const onRedirect = ({ params, route }) => navigate(getUrl(route, params, true));

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('BigButtonListReference - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId } }, true);
    }
  };

  const getDeleteSchemaIndex = index => referenceBigButtonsDispatch({ type: 'GET_DELETE_INDEX', payload: { index } });

  const onDeleteDatasetSchema = async () => {
    handleDialogs({ dialog: 'isDeleteDataset', isVisible: false });

    showLoading();
    try {
      await DatasetService.deleteSchema(dataflowState.designDatasetSchemas[deleteIndex].datasetId);
      onUpdateData();
      setUpdatedDatasetSchema(remove(dataflowState.updatedDatasetSchema, event => event.schemaIndex !== deleteIndex));
    } catch (error) {
      console.error('BigButtonListReference - onDeleteDatasetSchema.', error);
      if (error.response.status === 401) {
        notificationContext.add({ type: 'DELETE_DATASET_SCHEMA_LINK_ERROR' }, true);
      }
    } finally {
      hideLoading();
    }
  };

  const onUpload = async () => {
    onImportSchema(false);
    setImportingLoading(true);
    setIsImportingDataflow(true);
    notificationContext.add({
      type: 'IMPORT_DATASET_SCHEMA_INIT',
      content: { dataflowName: dataflowState.name }
    });
  };

  const onCreateReferenceDatasets = async () => {
    handleDialogs({ dialog: 'isCreateReference', isVisible: false });
    notificationContext.add({ type: 'CREATE_REFERENCE_DATASETS_INIT', content: {} });
    setIsCreatingReferenceDatasets(true);

    try {
      return await DataCollectionService.createReference(dataflowId, true);
    } catch (error) {
      console.error('BigButtonListReference - onCreateReferenceDatasets.', error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add(
        {
          type: 'CREATE_REFERENCE_DATASETS_ERROR',
          content: { customContent: { referenceDataflowId: dataflowId }, dataflowName }
        },
        true
      );
      setIsCreatingReferenceDatasets(false);
    } finally {
      handleDialogs({ dialog: 'isTableWithNoPK', isVisible: false });
      notificationContext.removeHiddenByKey('NO_PK_REFERENCE_DATAFLOW_ERROR_EVENT');
    }
  };

  const onCreateReferenceDatasetsWithNoPKs = async () => {
    handleDialogs({ dialog: 'isCreateReference', isVisible: false });
    notificationContext.add({ type: 'CREATE_REFERENCE_DATASETS_INIT', content: {} });
    setIsCreatingReferenceDatasets(true);

    try {
      return await DataCollectionService.createReference(dataflowId, false);
    } catch (error) {
      console.error('BigButtonListReference - onCreateReferenceDatasetsWithNoPKs.', error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add(
        {
          type: 'CREATE_REFERENCE_DATASETS_ERROR',
          content: { customContent: { referenceDataflowId: dataflowId }, dataflowName }
        },
        true
      );
      setIsCreatingReferenceDatasets(false);
    } finally {
      handleDialogs({ dialog: 'isTableWithNoPK', isVisible: false });
      notificationContext.removeHiddenByKey('NO_PK_REFERENCE_DATAFLOW_ERROR_EVENT');
    }
  };

  const onLoadSchemasValidations = async () => {
    const { data } = await DataflowService.getSchemasValidation(dataflowId);
    referenceBigButtonsDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { data } });
  };

  const newSchemaModel = [
    {
      command: () => handleDialogs({ dialog: 'isNewDataset', isVisible: true }),
      icon: 'add',
      label: resourcesContext.messages['createNewEmptyDatasetSchema']
    },
    {
      command: () => handleDialogs({ dialog: 'cloneDialogVisible', isVisible: true }),
      icon: 'clone',
      label: resourcesContext.messages['cloneSchemasFromDataflow']
    },
    {
      command: () => onImportSchema(true),
      icon: 'import',
      label: resourcesContext.messages['importSchema']
    }
  ];

  const designModel = newDatasetSchema => {
    return [
      {
        label: resourcesContext.messages['openDataset'],
        icon: 'openFolder',
        command: () => {
          onRedirect({
            route: routes.REFERENCE_DATASET_SCHEMA,
            params: { dataflowId, datasetId: newDatasetSchema.datasetId }
          });
        }
      },
      { label: resourcesContext.messages['rename'], icon: 'pencil' },
      {
        label: resourcesContext.messages['delete'],
        icon: 'trash',
        command: () => {
          getDeleteSchemaIndex(newDatasetSchema.index);
          handleDialogs({ dialog: 'isDeleteDataset', isVisible: true });
        }
      }
    ];
  };

  const referenceDatasetModels = isNil(dataflowState.data.referenceDatasets)
    ? []
    : dataflowState.data.referenceDatasets.map(referenceDataset => {
        return {
          layout: 'defaultBigButton',
          buttonClass: 'referenceDataset',
          buttonIcon: 'howTo',
          caption: referenceDataset.datasetSchemaName,
          handleRedirect: () => {
            onRedirect({
              route: routes.REFERENCE_DATASET,
              params: { dataflowId: dataflowId, datasetId: referenceDataset.datasetId }
            });
          },
          helpClassName: 'dataflow-dataset-container-help-step',
          model: [],
          onWheel: () =>
            onRedirect({
              route: routes.REFERENCE_DATASET,
              params: { dataflowId: dataflowId, datasetId: referenceDataset.datasetId }
            }),
          visibility: dataflowState.isCustodian || dataflowState.isCustodianUser
        };
      });

  const createReferenceDatasets = {
    buttonClass: 'newItem',
    buttonIcon: dataflowState.isCreatingReferenceDatasets ? 'spinner' : 'siteMap',
    buttonIconClass: dataflowState.isCreatingReferenceDatasets
      ? 'spinner'
      : hasDatasets && isCreateReferenceEnabled
      ? 'siteMap'
      : 'siteMapDisabled',
    caption: resourcesContext.messages['createReferenceDatasetsBtnLabel'],
    enabled: hasDatasets && isCreateReferenceEnabled,
    handleRedirect:
      hasDatasets && isCreateReferenceEnabled && !dataflowState.isCreatingReferenceDatasets
        ? () => handleDialogs({ dialog: 'isCreateReference', isVisible: true })
        : () => {},
    helpClassName: 'dataflow-create-datacollection-help-step',
    layout: 'defaultBigButton',
    tooltip: !hasDatasets
      ? resourcesContext.messages['createReferenceDatasetsBtnTooltip']
      : !isCreateReferenceEnabled
      ? resourcesContext.messages['disabledCreateDataCollectionSchemasWithError']
      : '',
    visibility: isDesignStatus && dataflowState.isCustodian
  };

  const newSchemaBigButton = {
    buttonClass: 'newItem',
    buttonIcon: isCloningDataflow || isImportingDataflow ? 'spinner' : 'plus',
    buttonIconClass: isCloningDataflow || isImportingDataflow ? 'spinner' : 'newItemCross',
    caption: resourcesContext.messages['newSchema'],
    helpClassName: 'dataflow-new-schema-help-step',
    layout: isCloningDataflow ? 'defaultBigButton' : 'menuBigButton',
    model: isCloningDataflow ? [] : newSchemaModel,
    visibility: isDesignStatus && dataflowState.isCustodian
  };

  const designDatasetButtons = isNil(dataflowState.data.designDatasets)
    ? []
    : dataflowState.data.designDatasets.map(newDatasetSchema => ({
        buttonClass: 'schemaDataset',
        buttonIcon: 'pencilRuler',
        caption: newDatasetSchema.datasetSchemaName,
        dataflowStatus: dataflowState.status,
        datasetSchemaInfo: dataflowState.updatedDatasetSchema,
        model: designModel(newDatasetSchema),
        handleRedirect: () =>
          onRedirect({
            route: routes.REFERENCE_DATASET_SCHEMA,
            params: { dataflowId, datasetId: newDatasetSchema.datasetId }
          }),
        helpClassName: 'dataflow-schema-help-step',
        index: newDatasetSchema.index,
        layout: 'defaultBigButton',
        onSaveName: onSaveName,
        placeholder: resourcesContext.messages['datasetSchemaNamePlaceholder'],
        visibility: isDesignStatus && dataflowState.isCustodian
      }));

  const bigButtonList = [
    ...designDatasetButtons,
    newSchemaBigButton,
    createReferenceDatasets,
    ...referenceDatasetModels
  ]
    .filter(button => button.visibility)
    .map(button => <BigButton key={button.caption} {...button} />);

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={`${styles.splitButtonWrapper} dataflow-big-buttons-help-step`}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      {dialogVisibility.isNewDataset && (
        <Dialog
          className={styles.dialog}
          header={resourcesContext.messages['newDatasetSchema']}
          onHide={() => handleDialogs({ dialog: 'isNewDataset', isVisible: false })}
          visible={dialogVisibility.isNewDataset}>
          <NewDatasetSchemaForm
            dataflowId={dataflowId}
            datasetSchemaInfo={dataflowState.updatedDatasetSchema}
            onCreate={() => handleDialogs({ dialog: 'isNewDataset', isVisible: false })}
            onUpdateData={onUpdateData}
            setNewDatasetDialog={onToggleNewDatasetDialog}
          />
        </Dialog>
      )}

      {dialogVisibility.isImportSchemaVisible && (
        <CustomFileUpload
          accept=".zip"
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dialogHeader={`${resourcesContext.messages['importSchema']}`}
          dialogOnHide={() => onImportSchema(false)}
          dialogVisible={dialogVisibility.isImportSchemaVisible}
          infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} .zip`}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          name="file"
          onError={onImportSchemaError}
          onUpload={onUpload}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DataflowConfig.importSchema, { dataflowId })}`}
        />
      )}

      {dialogVisibility.cloneDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={
            <Fragment>
              <Button
                className="p-button-primary p-button-animated-blink"
                disabled={isNil(cloneDataflow.id)}
                icon="plus"
                label={resourcesContext.messages['cloneSelectedDataflow']}
                onClick={() => cloneDatasetSchemas()}
              />
              <Button
                className="p-button-secondary p-button-animated-blink p-button-right-aligned"
                icon="cancel"
                label={resourcesContext.messages['close']}
                onClick={() => {
                  handleDialogs({ dialog: 'cloneDialogVisible', isVisible: false });
                  resetCloneSchemasFiltersState();
                }}
              />
            </Fragment>
          }
          header={resourcesContext.messages['dataflowsList']}
          onHide={() => {
            handleDialogs({ dialog: 'cloneDialogVisible', isVisible: false });
            resetCloneSchemasFiltersState();
          }}
          style={{ width: '95%' }}
          visible={dialogVisibility.cloneDialogVisible}>
          <CloneSchemas dataflowId={dataflowId} getCloneDataflow={getCloneDataflow} isReferenceDataflow />
        </Dialog>
      )}

      {dialogVisibility.isCreateReference && (
        <ConfirmDialog
          header={resourcesContext.messages['createReferenceDatasetsDialogHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onCreateReferenceDatasets}
          onHide={() => handleDialogs({ dialog: 'isCreateReference', isVisible: false })}
          visible={dialogVisibility.isCreateReference}>
          {resourcesContext.messages['createReferenceDatasetsDialogMessage']}
        </ConfirmDialog>
      )}

      {dialogVisibility.isTableWithNoPK && (
        <ConfirmDialog
          header={resourcesContext.messages['tableWithNoPKWarningTitle']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onCreateReferenceDatasetsWithNoPKs}
          onHide={() => {
            handleDialogs({ dialog: 'isTableWithNoPK', isVisible: false });
            notificationContext.removeHiddenByKey('NO_PK_REFERENCE_DATAFLOW_ERROR_EVENT');
          }}
          visible={dialogVisibility.isTableWithNoPK}>
          {resourcesContext.messages['tableWithNoPKWarningBody']}
        </ConfirmDialog>
      )}

      {dialogVisibility.isDeleteDataset && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['deleteReferenceDatasetDialogHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteDatasetSchema}
          onHide={() => handleDialogs({ dialog: 'isDeleteDataset', isVisible: false })}
          visible={dialogVisibility.isDeleteDataset}>
          {resourcesContext.messages['deleteReferenceDatasetDialogMessage']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
