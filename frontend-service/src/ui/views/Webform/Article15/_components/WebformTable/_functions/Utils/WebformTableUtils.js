const parseWebformData = data => {
  data.here = data.elements.map(element => ({ ...element }));

  return data;
};

export const WebformTableUtils = { parseWebformData };
