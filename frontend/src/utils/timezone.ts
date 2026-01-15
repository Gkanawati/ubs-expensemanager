import { format, parse } from "date-fns";
import { formatInTimeZone, toZonedTime } from "date-fns-tz";

/**
 * Application timezone - São Paulo, Brazil (UTC-3)
 */
export const APP_TIMEZONE = "America/Sao_Paulo";

/**
 * Formats a Date object to YYYY-MM-DD string in São Paulo timezone.
 * Use this when sending dates to the API.
 */
export const formatDateForApi = (date: Date): string => {
  return formatInTimeZone(date, APP_TIMEZONE, "yyyy-MM-dd");
};

/**
 * Formats a Date object to a display string in São Paulo timezone.
 * Returns format like "Jan 15, 2026"
 */
export const formatDateDisplay = (date: Date): string => {
  return formatInTimeZone(date, APP_TIMEZONE, "MMM dd, yyyy");
};

/**
 * Formats a date string (YYYY-MM-DD) to display format.
 * Treats the input as a São Paulo timezone date.
 */
export const formatDateStringDisplay = (dateString: string): string => {
  // Parse as São Paulo date (treat the date as local to São Paulo)
  const date = parse(dateString, "yyyy-MM-dd", new Date());
  return format(date, "MMM dd, yyyy");
};

/**
 * Formats a datetime string to display with time in São Paulo timezone.
 * Returns format like "Jan 15, 2026, 02:30 PM"
 */
export const formatDateTimeDisplay = (dateTimeString: string): string => {
  const date = new Date(dateTimeString);
  return formatInTimeZone(date, APP_TIMEZONE, "MMM dd, yyyy, hh:mm a");
};

/**
 * Parses a YYYY-MM-DD string to a Date object, treating it as a São Paulo date.
 * Use this when converting API dates to Date objects for the DatePicker.
 */
export const parseDateFromApi = (dateString: string): Date => {
  // Create date at midnight in São Paulo timezone
  return parse(dateString, "yyyy-MM-dd", new Date());
};

/**
 * Gets today's date in São Paulo timezone as YYYY-MM-DD string.
 */
export const getTodayInSaoPaulo = (): string => {
  return formatInTimeZone(new Date(), APP_TIMEZONE, "yyyy-MM-dd");
};

/**
 * Gets the current date/time as a Date object adjusted to São Paulo timezone.
 */
export const getNowInSaoPaulo = (): Date => {
  return toZonedTime(new Date(), APP_TIMEZONE);
};
