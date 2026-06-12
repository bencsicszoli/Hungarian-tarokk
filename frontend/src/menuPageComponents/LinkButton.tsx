import { Link } from "react-router-dom";

interface LinkButtonProps {
  whereToLink?: string;
  buttonText: string;
  onHandleClick?: () => void;
  fontStyle: string;
}

function LinkButton({
  whereToLink,
  buttonText,
  onHandleClick,
  fontStyle,
}: LinkButtonProps) {
  
  const button = (
    <button
      onClick={onHandleClick}
      className={`w-full text-[#2f4b3a] bg-green-300 hover:bg-green-500 focus:ring-4 focus:outline-none focus:ring-green-400 rounded-lg px-5 py-2.5 text-center ${fontStyle}`}
    >
      {buttonText}
    </button>
  );

  if (whereToLink) {
    return (
      <div>
        <Link to={whereToLink}>{button}</Link>
      </div>
    );
  }

  return <div>{button}</div>;
}

export default LinkButton;
