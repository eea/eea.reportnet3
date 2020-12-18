export const webformViewReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_CHANGE_TAB':
      return { ...state, isVisible: payload.isVisible };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_SINGLE_CALCULATED_DATA':
      // return { ...state, singlesCalculatedData: payload };
      return {
        ...state,
        singlesCalculatedData: [
          {
            id: '2',
            isPolicyMeasureEnvisaged: 'No',
            sectors: [
              {
                sectorAffected: 'Cross-sectoral',
                otherSectors: '',
                objectives: ['23'],
                otherObjectives: ['name2']
              },
              {
                sectorAffected: 'Agriculture',
                otherSectors: '113124',
                objectives: ['24'],
                otherObjectives: ['cfgsdfg', '222']
              }
            ],
            statusImplementation: 'Implemented',
            implementationPeriodStart: '2000',
            implementationPeriodFinish: '2020',
            implementationPeriodComment: 'ImplementPeriodComment',
            projectionsScenario: 'With additional measures',
            unionPolicyList: ['1', '2'],
            entities: [
              {
                type: 'National government',
                name: 'single1'
              },
              {
                type: 'Research institutions',
                name: '1'
              }
            ],
            policyImpacting: 'ESD/ESR',
            typePolicyInstrument: ['Economic'],
            ghgAffected: ['Carbon dioxide (CO2)']
          },
          {
            id: '3',
            isPolicyMeasureEnvisaged: 'Yes',
            sectors: [
              {
                sectorAffected: 'Energy consumption',
                otherSectors: '',
                objectives: ['23', '24'],
                otherObjectives: ['name3']
              }
            ],
            statusImplementation: 'Adopted',
            implementationPeriodStart: '2000',
            implementationPeriodFinish: '2021',
            implementationPeriodComment: 'ImplementPeriodComment 2',
            projectionsScenario: 'Without measures',
            unionPolicyList: ['3', '4'],
            entities: [
              {
                type: 'Others',
                name: 'single2'
              }
            ],
            policyImpacting: 'LULUCF',
            typePolicyInstrument: ['Information', 'Other'],
            ghgAffected: ['Nitrogen trifluoride (NF3)', 'Sulphur hexafluoride (SF6)']
          }
        ]
      };
    default:
      return state;
  }
};
