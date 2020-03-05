const reduceString = (text, prefix, suffix) => {
  let string = text;
  let index = string.indexOf(prefix);

  if (index >= 0) {
    string = string.substring(index + prefix.length);
  } else {
    return '';
  }
  if (suffix) {
    index = string.indexOf(suffix);
    if (index < 0) {
      return '';
    } else {
      string = string.substring(0, index);
    }
  }
  return string;
};

export const DataflowsUtils = {
  reduceString
};
