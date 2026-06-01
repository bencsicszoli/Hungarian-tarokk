interface InputFieldProps {
  htmlFor: string;
  labelText: string;
  inputType: string;
  inputName: string;
  inputId: string;
  placeholderText: string;
  inputValue: string;
  onInputValue: (value: string) => void;
  autoComplete: string;
}

function InputField({
  htmlFor,
  labelText,
  inputType,
  inputName,
  inputId,
  placeholderText,
  inputValue,
  onInputValue,
  autoComplete,
}: InputFieldProps) {
  
  return (
    <div>
      <label
        htmlFor={htmlFor}
        className="block mb-2 text-md font-medium text-green-100"
      >
        {labelText}
      </label>
      <input
        type={inputType}
        name={inputName}
        id={inputId}
        className="bg-green-300 border border-gray-300 text-[#2f4b3a] font-semibold placeholder:text-[#2f4b3a] placeholder:font-normal rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5"
        placeholder={placeholderText}
        value={inputValue}
        onChange={(e) => onInputValue(e.target.value)}
        required
        autoComplete={autoComplete}
      />
    </div>
  );
}

export default InputField;
