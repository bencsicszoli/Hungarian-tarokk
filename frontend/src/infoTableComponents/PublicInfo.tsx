import type { JSX } from "react";
import type { InfoLine } from "../types";

function PublicInfo({
  publicInfo,
  onDisplayInformation,
}: {
  publicInfo: InfoLine[];
  onDisplayInformation: (publicInfo: InfoLine[]) => JSX.Element[];
}) {
  if (publicInfo.length > 0) {
    return (
      <div className="w-full h-auto text-3xl px-4 py-6 font-bold flex justify-center items-center text-center">
        <p className="animate-jump-in" key={JSON.stringify(publicInfo)}>
          {onDisplayInformation(publicInfo)}
        </p>
      </div>
    );
  } else {
    return null;
  }
}

export default PublicInfo;
