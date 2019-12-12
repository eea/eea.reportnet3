const apiDataProvider = {
  all: async dataflowId => {
    const hardcodedResponseExample = {
      representativesOf: 'countries',
      dataProviders: [
        { dataProviderId: '1111', email: 'spain@es.es', name: 'Es' },
        { dataProviderId: '2222', email: 'germany@de.de', name: 'De' },
        { dataProviderId: '3333', email: 'greatbr@uk.uk', name: 'UK' },
        { dataProviderId: '4444', email: 'france@fr.fr', name: 'Fr' },
        { dataProviderId: '5555', email: 'italy@it.it', name: 'It' }
        /*       { dataProviderId: '7777', email: 'sony@sony.net', name: 'read_write' },
      { dataProviderId: '8888', email: 'sega@sega.net', name: 'read_write' },
      { dataProviderId: '9999', email: 'play@play.net', name: 'read_write' },
      { dataProviderId: '1010', email: 'sudo@sudo.net', name: 'read_write' },
      { dataProviderId: '0101', email: 'bash@bash.net', name: 'read_write' },
      { dataProviderId: '0000', email: 'pong@pong.net', name: 'read' } */
        /* { dataProviderId: '', email: '', name: '' } */
      ]
    };

    return hardcodedResponseExample;
  },
  allRepresentativesOf: async type => {
    let hardcodedResponseExample = [
      { dataProviderId: '1111', name: 'Es' },
      { dataProviderId: '2222', name: 'De' },
      { dataProviderId: '3333', name: 'UK' },
      { dataProviderId: '4444', name: 'Fr' },
      { dataProviderId: '5555', name: 'It' }
    ];

    let result = hardcodedResponseExample;

    return result;
  },
  add: (dataflowId, dataProviderEmail, dataProviderName) => {
    console.log(
      'Adding DataProvider to dataflowId: ',
      dataflowId,
      ' dataProviderEmail:',
      dataProviderEmail,
      ' name:',
      dataProviderName
    );
  },
  deleteById: (dataflowId, dataProviderId) => {
    console.log('Deliting DataProvider from dataflowId: ', dataflowId, ' dataProviderId', dataProviderId);
  },
  update: (dataflowId, dataProviderId, dataProviderEmail, dataProviderName) => {
    console.log(
      `Updating DataProvider from dataflowId: ${dataflowId}, dataProvider Id: ${dataProviderId}, new Role: ${(dataProviderEmail,
      dataProviderName)}`
    );
  }
};
export { apiDataProvider };
