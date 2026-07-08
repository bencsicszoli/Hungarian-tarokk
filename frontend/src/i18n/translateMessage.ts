import i18n from "./index";
import type { LocalizedMessage, InfoLine } from "../types";

export type { LocalizedMessage };

export function translateMessage(message: LocalizedMessage): string {
  return i18n.t(message.key, { ns: "messages", ...message.params });
}

export function translateMessages(messages: LocalizedMessage[]): string[] {
  return messages.map(translateMessage);
}

export function isLocalizedMessage(line: InfoLine): line is LocalizedMessage {
  return typeof line === "object" && line !== null && "key" in line;
}

export function translateInfoLine(line: InfoLine): string {
  return isLocalizedMessage(line) ? translateMessage(line) : line;
}

export function translateInfoLines(lines: InfoLine[]): string[] {
  return lines.map(translateInfoLine);
}
