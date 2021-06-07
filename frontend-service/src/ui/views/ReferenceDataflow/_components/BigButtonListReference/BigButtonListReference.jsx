import { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './BigButtonListReference.module.scss';

import { BigButton } from 'ui/views/_components/BigButton';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from 'ui/views/_components/NewDatasetSchemaForm';

import { DataCollectionService } from 'core/services/DataCollection';
import { DatasetService } from 'core/services/Dataset';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { referenceBigButtonsReducer } from './_functions/Reducers/referenceBigButtonsReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';

const BigButtonListReference = withRouter(
  ({ dataflowId, dataflowState, history, onSaveName, onUpdateData, setIsCreatingReferenceDatasets }) => {
    const { showLoading, hideLoading } = useContext(LoadingContext);

    const [isDesignStatus, setIsDesignStatus] = useState(false);
    const [hasDatasets, setHasDatasets] = useState(false);

    useEffect(() => {
      setIsDesignStatus(dataflowState.status === config.dataflowStatus.DESIGN);
    }, [dataflowState.status]);

    useEffect(() => {
      setHasDatasets(dataflowState.data?.designDatasets?.length);
    }, [dataflowState.data.designDatasets]);

    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);

    const [referenceBigButtonsState, referenceBigButtonsDispatch] = useReducer(referenceBigButtonsReducer, {
      deleteIndex: null,
      dialogVisibility: { isCreateReference: false, isDeleteDataset: false, isNewDataset: false }
    });

    const { dialogVisibility, deleteIndex } = referenceBigButtonsState;

    const handleDialogs = ({ dialog, isVisible }) => {
      referenceBigButtonsDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
    };

    const onToggleNewDatasetDialog = isVisible => handleDialogs({ dialog: 'isNewDataset', isVisible });

    const onRedirect = ({ params, route }) => history.push(getUrl(route, params, true));

    const getMetadata = async ids => {
      try {
        return await MetadataUtils.getMetadata(ids);
      } catch (error) {
        notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId } });
      }
    };

    const getDeleteSchemaIndex = index => referenceBigButtonsDispatch({ type: 'GET_DELETE_INDEX', payload: { index } });

    const onDeleteDatasetSchema = async () => {
      handleDialogs({ dialog: 'isDeleteDataset', isVisible: false });

      showLoading();
      try {
        const { status } = await DatasetService.deleteSchemaById(
          dataflowState.designDatasetSchemas[deleteIndex].datasetId
        );
        if (status >= 200 && status <= 299) {
          onUpdateData();
        }
      } catch (error) {
        console.error(error.response);
        if (error.response.status === 401) {
          notificationContext.add({ type: 'DELETE_DATASET_SCHEMA_LINK_ERROR' });
        }
      } finally {
        hideLoading();
      }
    };

    const onAddReferenceDataset = async () => {
      handleDialogs({ dialog: 'isCreateReference', isVisible: false });

      notificationContext.add({ type: 'CREATE_DATA_COLLECTION_INIT', content: {} });

      setIsCreatingReferenceDatasets(true);

      try {
        return await DataCollectionService.createReference(dataflowId);
      } catch (error) {
        console.error(error);
        const {
          dataflow: { name: dataflowName }
        } = await getMetadata({ dataflowId });

        notificationContext.add({
          type: 'CREATE_REFERENCE_DATASETS_ERROR',
          content: { referenceDataflowId: dataflowId, dataflowName }
        });
        setIsCreatingReferenceDatasets(false);
      }
    };

    const newSchemaModel = [
      {
        command: () => handleDialogs({ dialog: 'isNewDataset', isVisible: true }),
        icon: 'add',
        label: resources.messages['createNewEmptyDatasetSchema']
      },
      { disabled: true, icon: 'clone', label: resources.messages['cloneSchemasFromDataflow'] },
      { disabled: true, icon: 'import', label: resources.messages['importSchema'] }
    ];

    const designModel = newDatasetSchema => {
      return [
        {
          label: resources.messages['openDataset'],
          icon: 'openFolder',
          command: () => {
            onRedirect({
              route: routes.REFERENCE_DATASET_SCHEMA,
              params: { dataflowId, datasetId: newDatasetSchema.datasetId }
            });
          }
        },
        { label: resources.messages['rename'], icon: 'pencil' },
        {
          label: resources.messages['delete'],
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
                route: routes.DATASET,
                params: { dataflowId: dataflowId, datasetId: referenceDataset.datasetId }
              });
            },
            helpClassName: 'dataflow-dataset-container-help-step',
            model: [],
            onWheel: () =>
              onRedirect({
                route: routes.DATASET,
                params: { dataflowId: dataflowId, datasetId: referenceDataset.datasetId }
              }),
            visibility: true
          };
        });

    const createReferenceDatasets = {
      buttonClass: 'newItem',
      buttonIcon: dataflowState.isCreatingReferenceDatasets ? 'spinner' : 'siteMap',
      enabled: hasDatasets,
      buttonIconClass: dataflowState.isCreatingReferenceDatasets
        ? 'spinner'
        : hasDatasets
        ? 'siteMap'
        : 'siteMapDisabled',
      caption: resources.messages['createReferenceDatasetsBtnLabel'],
      handleRedirect:
        hasDatasets && !dataflowState.isCreatingReferenceDatasets
          ? () => handleDialogs({ dialog: 'isCreateReference', isVisible: true })
          : () => {},
      helpClassName: 'dataflow-create-datacollection-help-step',
      layout: 'defaultBigButton',
      tooltip: !hasDatasets ? resources.messages['createReferenceDatasetsBtnTooltip'] : '',
      visibility: isDesignStatus
    };

    const newSchemaBigButton = {
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      buttonIconClass: 'newItemCross',
      caption: resources.messages['newSchema'],
      helpClassName: 'dataflow-new-schema-help-step',
      layout: 'menuBigButton',
      model: newSchemaModel,
      visibility: isDesignStatus
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
          placeholder: resources.messages['datasetSchemaNamePlaceholder'],
          visibility: isDesignStatus
        }));

    const bigButtonList = [
      ...designDatasetButtons,
      newSchemaBigButton,
      createReferenceDatasets,
      ...referenceDatasetModels
    ].map(button => (button.visibility ? <BigButton key={button.caption} {...button} /> : null));

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
            header={resources.messages['newDatasetSchema']}
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

        {dialogVisibility.isCreateReference && (
          <ConfirmDialog
            header={resources.messages['createReferenceDatasetsDialogHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onAddReferenceDataset}
            onHide={() => handleDialogs({ dialog: 'isCreateReference', isVisible: false })}
            visible={dialogVisibility.isCreateReference}>
            {resources.messages['createReferenceDatasetsDialogMessage']}
          </ConfirmDialog>
        )}

        {dialogVisibility.isDeleteDataset && (
          <ConfirmDialog
            header={resources.messages['createReferenceDatasetsDialogHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onDeleteDatasetSchema}
            onHide={() => handleDialogs({ dialog: 'isDeleteDataset', isVisible: false })}
            visible={dialogVisibility.isDeleteDataset}>
            {'isDeleteDataset'}
          </ConfirmDialog>
        )}
      </Fragment>
    );
  }
);

export { BigButtonListReference };
