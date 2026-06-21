function EditPageField({
  htmlFor,
  labelText,
  inputType,
  inputName,
  placeholder,
  value,
  onChangeHandler,
  autoComplete,
  required,
}: {
  htmlFor: string;
  labelText: string;
  inputType: string;
  inputName: string;
  placeholder: string;
  value?: string;
  onChangeHandler: React.Dispatch<React.SetStateAction<string>>;
  autoComplete?: string;
  required?: boolean;
}) {
  return (
    <div className="flex items-center">
      <label
        htmlFor={htmlFor}
        className="pr-8 w-1/2 text-lg text-green-100 text-end"
      >
        {labelText}:
      </label>
      <input
        type={inputType}
        name={inputName}
        id={htmlFor}
        className="placeholder:text-center text-center w-1/2 bg-green-300 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-primary-600 focus:border-primary-600 p-2.5"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChangeHandler(e.target.value)}
        autoComplete={autoComplete}
        required={required}
      />
    </div>
  );
}

export default EditPageField;
