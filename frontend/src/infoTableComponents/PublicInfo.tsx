import type { JSX } from "react";

function PublicInfo({
  publicInfo,
  onDisplayInformation,
}: {
  publicInfo: string;
  onDisplayInformation: (publicInfo: string) => JSX.Element[];
}) {
  if (publicInfo) {
    return (
      <div className="w-full h-auto text-3xl px-4 py-6 font-bold flex justify-center items-center text-center">
        <p className="animate-jump-in" key={publicInfo}>
          {onDisplayInformation(publicInfo)}
        </p>
      </div>
    );
  } else {
    return null;
  }
}

export default PublicInfo;
