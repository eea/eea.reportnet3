const apiDataProvider = {
  all: async dataflowId => {
    const hardcodedResponseExample = [
      { id: '1111', email: 'ygryc@ygryc.net', name: 'Es' },
      { id: '2222', email: 'pedro@pedro.net', name: 'Ge' },
      { id: '3333', email: 'jose@jose.net', name: 'UK' },
      { id: '4444', email: 'rambo@rambo.com', name: 'Fr' },
      { id: '5555', email: 'ygryc@ygryc.net', name: 'It' },
      { id: '6666', email: 'pedro@pedro.net', name: 'read' },
      { id: '7777', email: 'sony@sony.net', name: 'read_write' },
      { id: '8888', email: 'sega@sega.net', name: 'read_write' },
      { id: '9999', email: 'play@play.net', name: 'read_write' },
      { id: '1010', email: 'sudo@sudo.net', name: 'read_write' },
      { id: '0101', email: 'bash@bash.net', name: 'read_write' },
      { id: '0000', email: 'pong@pong.net', name: 'read' }
    ];

    return hardcodedResponseExample;
  },
  add: async (dataflowId, dataProviderEmail, dataProviderName) => {
    console.log(
      'Adding DataProvider to dataflowId: ',
      dataflowId,
      ' dataProviderEmail:',
      dataProviderEmail,
      ' name:',
      dataProviderName
    );
  },
  deleteById: async (dataflowId, dataProviderId) => {
    console.log('Deliting DataProvider from dataflowId: ', dataflowId, ' dataProviderId', dataProviderId);
  },
  update: async (dataflowId, dataProviderId, dataProviderEmail, dataProviderName) => {
    console.log(
      `Updating DataProvider from dataflowId: ${dataflowId}, dataProvider Id: ${dataProviderId}, new Role: ${(dataProviderEmail,
      dataProviderName)}`
    );
  }
};
export { apiDataProvider };
