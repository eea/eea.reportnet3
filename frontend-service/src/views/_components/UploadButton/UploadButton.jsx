export const UploadControl = ({ accept, children, disabled, onChange, value, multiple }) => {
  return (
    <label htmlFor="contained-button-file" className="m-0 w-100">
      <input
        accept={accept}
        disabled={disabled}
        id="contained-button-file"
        multiple={multiple}
        onChange={disabled ? () => {} : onChange}
        style={{ display: 'none' }}
        type="file"
        value={value}
      />
      {children}
    </label>
  );
};
