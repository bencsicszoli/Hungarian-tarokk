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
  // render just a button when no destination URL is provided; otherwise wrap
  // with a <Link> so we can use react-router navigation for static links.
  const button = (
    <button
      onClick={onHandleClick}
      className={`w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 ${fontStyle} rounded-lg px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800 ${fontStyle}`}
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
