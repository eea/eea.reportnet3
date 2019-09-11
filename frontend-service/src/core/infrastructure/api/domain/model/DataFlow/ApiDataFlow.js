import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataFlow = {
  accept: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(config.acceptDataFlow.url, { dataFlowId, type: 'ACCEPTED' }),
      data: { id: dataFlowId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  all: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  accepted: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  completed: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  dashboards: async dataFlowId => {
    console.log('Getting all the dashboards', dataFlowId);
    // const tokens = userStorage.get();
    // const response = await HTTPRequester.get({
    //   url: '/jsons/dataCustodianDashboards.json',
    //   queryString: {},
    //   headers: {
    //     Authorization: `Bearer ${tokens.accessToken}`
    //   }
    // });
    // return response.data;
    const hardcodedDashboardTest = [
      {
        idDataSetSchema: '5d4abe555b1c1e0001477410',
        nameDataSetSchema: 'dataSet_1',
        dataSetCountries: [
          {
            countryName: 'Austria',
            countryCode: 'at',
            isDataSetReleased: true
          },
          {
            countryName: 'Belgium',
            countryCode: 'be',
            isDataSetReleased: true
          },
          {
            countryName: 'Bulgarian',
            countryCode: 'bg',
            isDataSetReleased: true
          },
          {
            countryName: 'Croatia',
            countryCode: 'hr',
            isDataSetReleased: false
          },
          {
            countryName: 'Cyprus',
            countryCode: 'cy',
            isDataSetReleased: true
          },
          {
            countryName: 'Czechia',
            countryCode: 'cz',
            isDataSetReleased: true
          },
          {
            countryName: 'Denmark',
            countryCode: 'dk',
            isDataSetReleased: true
          },
          {
            countryName: 'Estonia',
            countryCode: 'ee',
            isDataSetReleased: false
          },
          {
            countryName: 'Finland',
            countryCode: 'fi',
            isDataSetReleased: true
          },
          {
            countryName: 'France',
            countryCode: 'fr',
            isDataSetReleased: true
          },
          {
            countryName: 'Germany',
            countryCode: 'de',
            isDataSetReleased: true
          },
          {
            countryName: 'Greece',
            countryCode: 'el',
            isDataSetReleased: true
          },
          {
            countryName: 'Hungary',
            countryCode: 'hu',
            isDataSetReleased: true
          },
          {
            countryName: 'Ireland',
            countryCode: 'ie',
            isDataSetReleased: false
          },
          {
            countryName: 'Italy',
            countryCode: 'it',
            isDataSetReleased: true
          },
          {
            countryName: 'Latvia',
            countryCode: 'lv',
            isDataSetReleased: true
          },
          {
            countryName: 'Lithuania',
            countryCode: 'lt',
            isDataSetReleased: true
          },
          {
            countryName: 'Luxembourg',
            countryCode: 'lu',
            isDataSetReleased: true
          },
          {
            countryName: 'Malta',
            countryCode: 'mt',
            isDataSetReleased: false
          },
          {
            countryName: 'Netherlands',
            countryCode: 'nl',
            isDataSetReleased: true
          },
          {
            countryName: 'Poland',
            countryCode: 'pl',
            isDataSetReleased: true
          },
          {
            countryName: 'Portugal',
            countryCode: 'pt',
            isDataSetReleased: true
          },
          {
            countryName: 'Romania',
            countryCode: 'ro',
            isDataSetReleased: false
          },
          {
            countryName: 'Slovakia',
            countryCode: 'sk',
            isDataSetReleased: true
          },
          {
            countryName: 'Slovenia',
            countryCode: 'sl',
            isDataSetReleased: false
          },
          {
            countryName: 'Spain',
            countryCode: 'es',
            isDataSetReleased: false
          },
          {
            countryName: 'Sweeden',
            countryCode: 'se',
            isDataSetReleased: true
          },
          {
            countryName: 'United Kingdom',
            countryCode: 'uk',
            isDataSetReleased: true
          }
        ],
        tableSchemas: [
          {
            idTableSchema: '5d4abe555b1c1e0001477412',
            nameTableSchema: 'BWQD_2006_IdentifiedBW',
            tableStatus: {
              error: [
                {
                  name: 'Austria',
                  code: 'at'
                },
                {
                  name: 'Belgium',
                  code: 'be'
                },
                {
                  name: 'Bulgarian',
                  code: 'bg'
                },
                {
                  name: 'Croatia',
                  code: 'hr'
                },
                {
                  name: 'Cyprus',
                  code: 'cy'
                },
                {
                  name: 'Czechia',
                  code: 'cz'
                }
              ],
              warning: [
                {
                  name: 'Denmark',
                  code: 'dk'
                },
                {
                  name: 'Estonia',
                  code: 'ee'
                },
                {
                  name: 'Finland',
                  code: 'fi'
                },
                {
                  name: 'France',
                  code: 'fr'
                },
                {
                  name: 'Germany',
                  code: 'de'
                },
                {
                  name: 'Greece',
                  code: 'el'
                },
                {
                  name: 'Hungary',
                  code: 'hu'
                },
                {
                  name: 'Ireland',
                  code: 'ie'
                },
                {
                  name: 'Italy',
                  code: 'it'
                },
                {
                  name: 'Latvia',
                  code: 'lv'
                },
                {
                  name: 'Lithuania',
                  code: 'lt'
                }
              ],
              correct: [
                {
                  name: 'Luxembourg',
                  code: 'lu'
                },
                {
                  name: 'Malta',
                  code: 'mt'
                },
                {
                  name: 'Netherlands',
                  code: 'nl'
                },
                {
                  name: 'Poland',
                  code: 'pl'
                },
                {
                  name: 'Portugal',
                  code: 'pt'
                },
                {
                  name: 'Romania',
                  code: 'ro'
                },
                {
                  name: 'Slovakia',
                  code: 'sk'
                },
                {
                  name: 'Slovenia',
                  code: 'sl'
                },
                {
                  name: 'Spain',
                  code: 'es'
                },
                {
                  name: 'Sweeden',
                  code: 'se'
                },
                {
                  name: 'United Kingdom',
                  code: 'uk'
                }
              ]
            }
          }
        ]
      }
    ];
    console.log('hardcodedDashboardTest', hardcodedDashboardTest);
    return hardcodedDashboardTest;
  },
  pending: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  reject: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(config.rejectDataFlow.url, { dataFlowId, type: 'REJECTED' }),
      data: { id: dataFlowId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  reporting: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_DataflowById.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
