import type { User, GameState } from "./Types";

export function createPrivateInfo(
  selectedTarokkNumber: number,
  calledTarokk: string | null,
  selectedBonuses: string[],
) {
  let privateInfo = "";
  if (selectedTarokkNumber > 0) {
    privateInfo += `You announced ${selectedTarokkNumber} tarokks!`;
  }
  if (calledTarokk) {
    privateInfo += ` ${calledTarokk}!`;
  }
  if (selectedBonuses.length > 0) {
    privateInfo += ` Selected bonuses: ${selectedBonuses.join(", ")}!`;
  }
  return privateInfo;
}