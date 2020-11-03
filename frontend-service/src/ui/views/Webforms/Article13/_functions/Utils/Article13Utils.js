import lowerFirst from 'lodash/lowerFirst';

const getTypeList = (records = []) => {
  const typeList = records.map(record => {
    let data = {};

    record.elements.forEach(element => (data = { ...data, [lowerFirst(element.name)]: element.value }));

    return data;
  });

  return {
    single: typeList.filter(list => list.isGroup === 'Single'),
    group: typeList.filter(list => list.isGroup === 'Group')
  };
};

export const Article13Utils = { getTypeList };
