import lowerFirst from 'lodash/lowerFirst';

const getTypeList = (records = []) => {
  const typeList = records.map(record => {
    let data = {};

    record.elements.forEach(
      element => (data = { ...data, [lowerFirst(element.name)]: element.value, recordId: record.recordId })
    );
    return data;
  });

  return {
    single: typeList.filter(list => list.isGroup === 'Single').sort((a, b) => a.id - b.id),
    group: typeList.filter(list => list.isGroup === 'Group').sort((a, b) => a.id - b.id)
  };
};

export const Article13Utils = { getTypeList };
