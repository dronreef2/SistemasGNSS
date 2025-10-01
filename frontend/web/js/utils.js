/**
 * Utility functions for date handling and validation
 */

/**
 * Check if a year is a leap year
 * @param {number} year - The year to check
 * @returns {boolean} True if leap year
 */
export function isLeapYear(year) {
  return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
}

/**
 * Get the maximum day for a given year (365 or 366)
 * @param {number} year - The year
 * @returns {number} 365 or 366
 */
export function getMaxDayOfYear(year) {
  return isLeapYear(year) ? 366 : 365;
}

/**
 * Convert a Date object to Julian day (day of year)
 * @param {Date} date - The date to convert
 * @returns {number} Julian day (1-366)
 */
export function dateToJulianDay(date) {
  const start = new Date(date.getFullYear(), 0, 1);
  const diff = date - start;
  return Math.floor(diff / 86400000) + 1;
}

/**
 * Convert year and Julian day to a Date object
 * @param {number} year - The year
 * @param {number} julianDay - The Julian day (1-366)
 * @returns {Date} The corresponding date
 */
export function julianDayToDate(year, julianDay) {
  const date = new Date(year, 0, 1);
  date.setDate(julianDay);
  return date;
}

/**
 * Validate year and day inputs
 * @param {number} year - The year to validate
 * @param {number} day - The day to validate
 * @returns {{valid: boolean, message: string}} Validation result
 */
export function validateDate(year, day) {
  const currentYear = new Date().getFullYear();
  
  if (!year || isNaN(year)) {
    return { valid: false, message: 'Ano inválido' };
  }
  
  if (year < 1995) {
    return { valid: false, message: 'Ano deve ser >= 1995' };
  }
  
  if (year > currentYear) {
    return { valid: false, message: `Ano deve ser <= ${currentYear}` };
  }
  
  if (!day || isNaN(day)) {
    return { valid: false, message: 'Dia inválido' };
  }
  
  if (day < 1) {
    return { valid: false, message: 'Dia deve ser >= 1' };
  }
  
  const maxDay = getMaxDayOfYear(year);
  if (day > maxDay) {
    return { valid: false, message: `Dia deve ser <= ${maxDay} (ano ${isLeapYear(year) ? 'bissexto' : 'não bissexto'})` };
  }
  
  return { valid: true, message: '' };
}

/**
 * Pad a number with leading zeros
 * @param {number} num - The number to pad
 * @param {number} length - The desired length
 * @returns {string} Padded string
 */
export function pad(num, length) {
  return String(num).padStart(length, '0');
}
