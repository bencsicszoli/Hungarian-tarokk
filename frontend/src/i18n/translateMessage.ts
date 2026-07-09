import i18n from "./index";
import type { LocalizedMessage, InfoLine } from "../types";

export type { LocalizedMessage };

function isLocalizedMessageValue(value: unknown): value is LocalizedMessage {
  return typeof value === "object" && value !== null && "key" in value;
}

function resolveParamValue(value: unknown): unknown {
  if (isLocalizedMessageValue(value)) {
    return translateMessage(value);
  }
  if (Array.isArray(value) && value.every(isLocalizedMessageValue)) {
    return value.map(translateMessage).join(", ");
  }
  return value;
}

export function translateMessage(message: LocalizedMessage): string {
  const params = message.params
    ? Object.fromEntries(
        Object.entries(message.params).map(([key, value]) => [
          key,
          resolveParamValue(value),
        ]),
      )
    : undefined;
  return i18n.t(message.key, { ns: "messages", ...params });
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
