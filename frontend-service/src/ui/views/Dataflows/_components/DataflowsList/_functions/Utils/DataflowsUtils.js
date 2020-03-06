const isDuplicatedInObject = (array, property) => {
  let isDuplicated = false,
    testObject = {};

  array.map(item => {
    const itemPropertyName = item[property];
    if (itemPropertyName in testObject) {
      testObject[itemPropertyName].duplicatedRoles = true;
      item.duplicatedRoles = true;
      isDuplicated = true;
    } else {
      testObject[itemPropertyName] = item;
      delete item.duplicatedRoles;
    }
  });
  return isDuplicated;
};

const reduceString = (text, prefix, suffix) => {
  let index = text.indexOf(prefix);
  if (index >= 0) {
    text = text.substring(index + prefix.length);
  } else {
    return '';
  }
  if (suffix) {
    index = text.indexOf(suffix);
    if (index < 0) {
      return '';
    } else {
      text = text.substring(0, index);
    }
  }
  return text;
};

export const DataflowsUtils = {
  isDuplicatedInObject,
  reduceString
};
