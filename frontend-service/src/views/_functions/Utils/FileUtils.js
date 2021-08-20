const formatBytes = bytes => {
  if (bytes === 0) return '0 B';

  const k = 1024;
  const sizeTypes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const decimals = i !== 0 ? 2 : 0;

  const bytesParsed = parseFloat(bytes / k ** i).toFixed(decimals);

  const result = { bytesParsed, sizeType: sizeTypes[i] };

  return result;
};

export const FileUtils = { formatBytes };
