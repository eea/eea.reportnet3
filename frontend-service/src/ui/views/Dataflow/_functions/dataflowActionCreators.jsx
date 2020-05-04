export function dataflowActionCreators(dataflowDispatch) {
  const initialLoad = (dataflow, isRepresentativeView) =>
    dataflowDispatch({
      type: 'INITIAL_LOAD',
      payload: {
        data: dataflow,
        description: dataflow.description,
        isRepresentativeView: isRepresentativeView,
        name: dataflow.name,
        obligations: dataflow.obligation,
        status: dataflow.status
      }
    });

  const loadPermissions = (hasWritePermissions, isCustodian) =>
    dataflowDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian } });

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const onDeleteDataflow = event =>
    dataflowDispatch({ type: 'ON_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const onEditData = (newName, newDescription) =>
    dataflowDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isEditDialogVisible: false }
    });

  const setDataProviderId = id => dataflowDispatch({ type: 'SET_DATA_PROVIDER_ID', payload: { id } });

  const setDatasetIdToSnapshotProps = id =>
    dataflowDispatch({ type: 'SET_DATASET_ID_TO_SNAPSHOT_PROPS', payload: { id } });

  const setDesignDatasetSchemas = designDatasets =>
    dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets } });

  const setFormHasRepresentatives = value =>
    dataflowDispatch({ type: 'SET_FORM_HAS_REPRESENTATIVES', payload: { formHasRepresentatives: value } });

  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDispatch({
      type: 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const setIsDataSchemaCorrect = validationResult =>
    dataflowDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { validationResult } });

  const setUrlRepresentativeId = id => dataflowDispatch({ type: 'SET_URL_REPRESENTATIVE_ID', payload: { id } });

  const setIsDataUpdated = () => dataflowDispatch({ type: 'SET_IS_DATA_UPDATED' });

  const setIsPageLoading = isPageLoading =>
    dataflowDispatch({ type: 'SET_IS_PAGE_LOADING', payload: { isPageLoading } });

  const setIsRepresentativeView = isRepresentativeView =>
    dataflowDispatch({ type: 'SET_IS_REPRESENTATIVE_VIEW', payload: { isRepresentativeView } });

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  const setIsReceiptLoading = isReceiptLoading => {
    dataflowDispatch({
      type: 'SET_IS_RECEIPT_LOADING',
      payload: { isReceiptLoading }
    });
  };
  const onCleanUpReceipt = () => {
    dataflowDispatch({
      type: 'ON_CLEAN_UP_RECEIPT',
      payload: { isReceiptLoading: false, isReceiptOutdated: false }
    });
  };

  return {
    initialLoad,
    loadPermissions,
    manageDialogs,
    onCleanUpReceipt,
    onDeleteDataflow,
    onEditData,
    setDataProviderId,
    setDatasetIdToSnapshotProps,
    setDesignDatasetSchemas,
    setFormHasRepresentatives,
    setHasRepresentativesWithoutDatasets,
    setIsDataSchemaCorrect,
    setIsDataUpdated,
    setIsPageLoading,
    setIsReceiptLoading,
    setUpdatedDatasetSchema,
    setUrlRepresentativeId
  };
}
