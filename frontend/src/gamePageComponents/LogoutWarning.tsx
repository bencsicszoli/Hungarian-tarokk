function LogoutWarning({
  onFormatLogoutWarning,
  onSetLogoutWarning,
  onConfirmLeaving,
}: {
  onFormatLogoutWarning: () => React.JSX.Element[] | null;
  onSetLogoutWarning: (info: string | null) => void;
  onConfirmLeaving: () => void;
}) {
  return (
    <div className="w-full h-[95%] border-black border-2 bg-green-300 text-[#2f4b3a] rounded-xl">
      <div className="w-full h-1/2">
        <p className="text-center text-3xl font-bold pt-4 px-2">
          {onFormatLogoutWarning()}
        </p>
      </div>
      <div className="w-full h-1/2 flex items-center justify-around">
        <button
          className="w-40 h-15 text-2xl bg-[#2f4b3a] hover:bg-green-700 cursor-pointer text-green-300 font-bold py-2 px-4 rounded-lg"
          onClick={onConfirmLeaving}
        >
          Yes
        </button>
        <button
          className="w-40 h-15 text-2xl bg-red-600 hover:bg-red-700 cursor-pointer text-red-100 font-bold py-2 px-4 rounded-lg"
          onClick={() => onSetLogoutWarning(null)}
        >
          No
        </button>
      </div>
    </div>
  );
}

export default LogoutWarning;
