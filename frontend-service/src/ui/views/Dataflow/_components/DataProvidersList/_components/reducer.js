const reducer = (state, action) => {
  let newState;
  switch (action.type) {
    case 'ADD_DATAPROVIDER':
      newState = { ...state, name: action.payload.name, email: action.payload.email };

      onDataProviderAdd(newState.email, newState.name);

      return newState;

    case 'DELETE_DATAPROVIDER':
      newState = { ...state, name: '', dataProviderId: action.payload };

      onDataProviderDelete(newState.dataProviderId);

      return newState;

    case 'UPDATE_TO_READ':
      newState = { name: 'read', dataProviderId: action.payload };

      onDataProviderRoleUpdate(newState.dataProviderId, newState.name);

      return newState;

    case 'UPDATE_TO_READ_WRITE':
      newState = { name: 'read_write', dataProviderId: action.payload };

      onDataProviderRoleUpdate(newState.dataProviderId, newState.name);

      return newState;

    default:
      return state;
  }
};

const onDataProviderRoleUpdate = async (dataProviderId, newRole) => {
  await DataProviderService.update(dataflowId, dataProviderId, newRole);
};

const onDataProviderAdd = async (email, name) => {
  await DataProviderService.add(dataflowId, email, name);
};

const onDataProviderDelete = async dataProviderId => {
  await DataProviderService.deleteById(dataflowId, dataProviderId);
};

export { reducer };
