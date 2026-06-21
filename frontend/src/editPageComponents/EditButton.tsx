function EditButton({
  onHandle,
  type,
  buttonText,
}: {
  onHandle: () => void;
  type: "submit" | "button";
  buttonText: string;
}) {
  return (
    <button
      onClick={onHandle}
      type={type}
      className="w-full text-[#2f4b3a] bg-green-300 hover:bg-green-500 focus:ring-4 focus:outline-none focus:ring-blue-300 font-bold rounded-lg text-md px-5 py-2.5 text-center"
    >
      {buttonText}
    </button>
  );
}

export default EditButton;
