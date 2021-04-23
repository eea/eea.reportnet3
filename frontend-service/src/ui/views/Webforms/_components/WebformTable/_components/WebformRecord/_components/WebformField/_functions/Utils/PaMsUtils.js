const getObjectiveOptions = value => {
  switch (value) {
    case 'Agriculture':
      return [
        'Reduction of fertilizer/manure use on cropland',
        'Other activities improving cropland management',
        'Improved livestock management',
        'Improved animal waste management systems',
        'Activities improving grazing land or grassland management',
        'Improved management of organic soils',
        'Other agriculture'
      ];

    case 'Cross-sectoral':
      return [];

    case 'Energy consumption':
      return [
        'Efficiency improvements of buildings',
        'Efficiency improvement of appliances',
        'Efficiency improvement in services/tertiary sector',
        'Efficiency improvement in industrial end-use sectors',
        'Demand management/reduction',
        'Other energy consumption'
      ];

    case 'Energy supply':
      return [
        'Increase in renewable energy sources in the electricity sector',
        'Increase in renewable energy in the heating and cooling sector',
        'Switch to less carbon-intensive fuels',
        'Enhanced non-renewable low carbon generation (nuclear)',
        'Reduction of losses',
        'Efficiency improvement in the energy and transformation sector',
        'Carbon capture and storage or carbon capture and utilization',
        'Control of fugitive emissions from energy production',
        'Other energy supply'
      ];

    case 'Industrial processes':
      return [
        'Installation if abatement technologies',
        'Improved control of fugitive emissions from industrial processes',
        'Improved control of manufacturing-fugitive and disposal emissions of fluorinated gases',
        'Replacement of fluorinated gases by gases with a lower GWP value',
        'Other industrial processes'
      ];

    case 'Land use land-use change and forestry':
      return [
        'Afforestation and reforestation',
        'Conservation of carbon in existing forests',
        'Enhancing production in existing forests',
        'Increasing the harvested wood products pool',
        'Enhanced forest management',
        'Prevention of deforestation',
        'Strengthening protection against natural disturbances',
        'Substitution of GHG intensive feedstocks and materials with harvested wood products',
        'Prevention of drainage or rewetting of wetlands',
        'Restoration of degraded lands',
        'Other land use/land-use and forestry'
      ];

    case 'Other sectors':
      return ['Other objectives'];

    case 'Transport':
      return [
        'Efficiency improvements of vehicles',
        'Modal shift to public transport or non-motorized transport',
        'Low carbon fuels',
        'Electric road transport',
        'Demand management/reduction',
        'Improved behavior',
        'Improved transport infrastructure',
        'Reduce emissions from international air or maritime transport',
        'Other transport'
      ];

    case 'Waste management/waste':
      return [
        'Demand management/reduction',
        'Enhanced recycling',
        'Enhanced CH4 collection and use',
        'Improved treatment technologies',
        'Improved landfill management',
        'Waste incineration with energy use',
        'Improved wastewater management systems',
        'Reduced landfilling',
        'Other waste'
      ];

    default:
      return [];
  }
};

export const PaMsUtils = { getObjectiveOptions };
