export const apiContributor = {
  all: async dataflowId => {
    /* 
        const response = await HTTPRequester.get({
          url: '/jsons/contributors.json',
          queryString: {}
        });
        return response.data; */

    const hardcodedResponseExample = [
      { id: '1111', login: 'ygryc@ygryc.net', role: 'read_write' },
      { id: '2222', login: 'pedro@pedro.net', role: 'read' },
      { id: '3333', login: 'jose@jose.net', role: 'read_write' },
      { id: '4444', login: 'rambo@rambo.com', role: 'read' },
      { id: '5555', login: 'ygryc@ygryc.net', role: 'read_write' },
      { id: '6666', login: 'pedro@pedro.net', role: 'read' },
      { id: '7777', login: 'sony@sony.net', role: 'read_write' },
      { id: '8888', login: 'sega@sega.net', role: 'read_write' },
      { id: '9999', login: 'play@play.net', role: 'read_write' },
      { id: '1010', login: 'sudo@sudo.net', role: 'read_write' },
      { id: '0101', login: 'bash@bash.net', role: 'read_write' },
      { id: '0000', login: 'pong@pong.net', role: 'read' }
    ];

    return hardcodedResponseExample;
  },
  addByLogin: async (dataflowId, contributorLogin, contributorRole) => {
    console.log(
      'Adding Contributor to dataflowId: ',
      dataflowId,
      ' contributorLogin:',
      contributorLogin,
      ' role:',
      contributorRole
    );
  },
  deleteById: async (dataflowId, contributorId) => {
    console.log('Deliting Contributor from dataflowId: ', dataflowId, ' contributorId', contributorId);
  },
  updateById: async (dataflowId, contributorId, newRole) => {
    console.log(
      `Updating Contributor from dataflowId: ${dataflowId}, contributor Id: ${contributorId}, new Role: ${newRole}`
    );
  }
};
