const parseWebformData = data => {
  data.elementsRecords = data.elements.map(element => ({ ...element }));

  return data;
};

export const WebformTableUtils = { parseWebformData };
