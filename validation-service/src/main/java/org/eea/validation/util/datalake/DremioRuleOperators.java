package org.eea.validation.util.datalake;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.PatternSyntaxException;

public class DremioRuleOperators {

    /** The Constant DATE_FORMAT. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** The Constant DATETIME_FORMAT. */
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean recordIfThen(boolean argIf, boolean argThen) {
        return !argIf || argThen;
    }

    /**
     * Record and.
     *
     * @param condition1 the condition 1
     * @param condition2 the condition 2
     * @return true, if successful
     */
    public static boolean recordAnd(boolean condition1, boolean condition2) {
        return condition1 && condition2;
    }

    /**
     * Record or.
     *
     * @param condition1 the condition 1
     * @param condition2 the condition 2
     * @return true, if successful
     */
    public static boolean recordOr(boolean condition1, boolean condition2) {
        return condition1 || condition2;
    }

    /**
     * Record not.
     *
     * @param condition the condition
     * @return true, if successful
     */
    public static boolean recordNot(boolean condition) {
        return !condition;
    }

    /**
     * Record null.
     *
     * @param value
     * @return true, if successful
     */
    public static boolean recordNull(String value) {
        return value.isEmpty();
    }

    /**
     * Record not null.
     *
     * @param value
     * @return true, if successful
     */
    public static boolean recordNotNull(String value) {
        return value.isEmpty();
    }

    /**
     * Record number equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberEquals(String value, Number number) {
        try {
            return Double.valueOf(value).equals(number.doubleValue());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberDistinct(String value, Number number) {
        try {
            return !Double.valueOf(value).equals(number.doubleValue());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberGreaterThan(String value, Number number) {
        try {
            return Double.valueOf(value) > number.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberLessThan(String value, Number number) {
        try {
            return Double.valueOf(value) < number.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberGreaterThanOrEqualsThan(String value, Number number) {
        try {
            return Double.valueOf(value) >= number.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordNumberLessThanOrEqualsThan(String value, Number number) {
        try {
            return Double.valueOf(value) <= number.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberEqualsRecord(String value1, String value2) {
        try {
            return Double.valueOf(value1).equals(Double.valueOf(value2));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberDistinctRecord(String value1, String value2) {
        try {
            return !Double.valueOf(value1).equals(Double.valueOf(value2));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberGreaterThanRecord(String value1, String value2) {
        try {
            return Double.valueOf(value1) > Double.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberLessThanRecord(String value1, String value2) {
        try {
            return  Double.valueOf(value1) < Double.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberGreaterThanOrEqualsThanRecord(String value1, String value2) {
        try {
            return Double.valueOf(value1) >= Double.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordNumberLessThanOrEqualsThanRecord(String value1, String value2) {
        try {
            return Double.valueOf(value1) <= Double.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record number matches.
     *
     * @param value the value
     * @param regex the regex
     * @param countryCode the countryCode
     * @return true, if successful
     */
    public static boolean recordNumberMatches(String value, String regex, String countryCode) {
        try {
            return value.isEmpty() || value.matches(replaceKeywords(regex, countryCode));
        } catch (PatternSyntaxException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length.
     *
     * @param value
     * @return the string
     */
    public static Integer recordStringLength(String value) {
        try {
            return value.length();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Record string length equals.
     *
     * @param value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthEquals(String value, Number number) {
        try {
            return value.length() == number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthDistinct(String value, Number number) {
        try {
            return value.length() != number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthGreaterThan(String value, Number number) {
        try {
            return value.length() > number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthLessThan(String value, Number number) {
        try {
            return value.length() < number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthGreaterThanOrEqualsThan(String value, Number number) {
        try {
            return value.length() >= number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean recordStringLengthLessThanOrEqualsThan(String value, Number number) {
        try {
            return value.length() <= number.intValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthEqualsRecord(String value1, String value2) {
        try {
            return value1.length() == Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthDistinctRecord(String value1, String value2) {
        try {
            return value1.length() != Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthGreaterThanRecord(String value1, String value2) {
        try {
            return value1.length() > Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthLessThanRecord(String value1, String value2) {
        try {
            return value1.length() < Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthGreaterThanOrEqualsThanRecord(String value1, String value2) {
        try {
            return value1.length() >= Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string length less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringLengthLessThanOrEqualsThanRecord(String value1, String value2) {
        try {
            return value1.length() <= Integer.valueOf(value2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string equals.
     *
     * @param value1
     * @param otherString the other string
     * @return true, if successful
     */
    public static boolean recordStringEquals(String value1, String otherString) {
        try {
            return value1.equals(otherString);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string equals ignore case.
     *
     * @param value1
     * @param otherString the other string
     * @return true, if successful
     */
    public static boolean recordStringEqualsIgnoreCase(String value1, String otherString) {
        try {
            return value1.equalsIgnoreCase(otherString);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string matches.
     *
     * @param value1
     * @param regex the regex
     * @param countryCode
     * @return true, if successful
     */
    public static boolean recordStringMatches(String value, String regex, String countryCode) {
        try {
            return value.isEmpty() || value.matches(replaceKeywords(regex, countryCode));
        } catch (PatternSyntaxException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record string equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringEqualsRecord(String value1, String value2) {
        return value1.equals(value2);
    }

    /**
     * Record string equals ignore case record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordStringEqualsIgnoreCaseRecord(String value1, String value2) {
        return value1.equalsIgnoreCase(value2);
    }

    /**
     * Record string matches record.
     *
     * @param value1
     * @param value2
     * @param countryCode
     * @return true, if successful
     */
    public static boolean recordStringMatchesRecord(String value1, String value2, String countryCode) {
        return value1.isEmpty() || value1.matches(replaceKeywords(value2, countryCode));
    }



    /**
     * Record day equals.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayEquals(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() == day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() == day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record day distinct.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayDistinct(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() != day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() != day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record day greater than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayGreaterThan(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() > day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() > day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record day less than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayLessThan(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() < day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() < day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record day greater than or equals than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayGreaterThanOrEqualsThan(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() >= day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() >= day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record day less than or equals than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDayLessThanOrEqualsThan(String value, Number day) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() <= day.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getDayOfMonth() <= day.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }



    /**
     * Record day equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() == date2.getDayOfMonth();
    }



    /**
     * Record day distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() != date2.getDayOfMonth();
    }



    /**
     * Record day greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() > date2.getDayOfMonth();
    }



    /**
     * Record day less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() < date2.getDayOfMonth();
    }



    /**
     * Record day greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() >= date2.getDayOfMonth();
    }



    /**
     * Record day less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() <= date2.getDayOfMonth();
    }

    /**
     * Record day equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() == number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() == number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record day distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() != number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() != number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record day greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() > number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() > number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record day less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() < number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() < number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record day greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() >= number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() >= number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record day less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDayLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() <= number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getDayOfMonth() <= number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month equals.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthEquals(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() == month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                        value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() == month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month distinct.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthDistinct(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() != month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                        value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() != month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month greater than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThan(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() > month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                        value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() > month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month less than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthLessThan(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() < month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                        value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() < month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month greater than or equals than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThanOrEqualsThan(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() >= month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                       value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() >= month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month less than or equals than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthLessThanOrEqualsThan(String value, Number month) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() <= month.longValue();
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime fieldDate = LocalDateTime.parse(
                        value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                return fieldDate.getMonthValue() <= month.longValue();
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }



    /**
     * Record month equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() == date2.getMonthValue();
    }



    /**
     * Record month distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() != date2.getMonthValue();
    }



    /**
     * Record month greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() > date2.getMonthValue();
    }



    /**
     * Record month less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() < date2.getMonthValue();
    }



    /**
     * Record month greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() >= date2.getMonthValue();
    }



    /**
     * Record month less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() <= date2.getMonthValue();
    }

    /**
     * Record month equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() == number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() == number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() != number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() != number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record month greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() > number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() > number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() < number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() < number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() >= number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() >= number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record month less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() <= number;
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime date = LocalDateTime.parse(
                        value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
                int number = Integer.parseInt(value2);
                return date.getMonthValue() <= number;
            } catch (Exception ex) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year equals.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearEquals(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() == year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year distinct.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearDistinct(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() != year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year greater than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearGreaterThan(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() > year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year less than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearLessThan(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() < year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year greater than or equals than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearGreaterThanOrEqualsThan(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() >= year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year less than or equals than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYearLessThanOrEqualsThan(String value, Number year) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() <= year.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() == date2.getYear();
    }

    /**
     * Record year distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() != date2.getYear();
    }



    /**
     * Record year greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() > date2.getYear();
    }



    /**
     * Record year less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() < date2.getYear();
    }



    /**
     * Record year greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() >= date2.getYear();
    }



    /**
     * Record year less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() <= date2.getYear();
    }

    /**
     * Record year equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() == number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() != number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() > number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() < number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() >= number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record year less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYearLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDate date = LocalDate.parse(value1, DATE_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() <= number;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date equals.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateEquals(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date distinct.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateDistinct(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return !fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date greater than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateGreaterThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isAfter(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date less than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateLessThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isBefore(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date less than or equals than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateLessThanOrEqualsThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date greater than or equals than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDateGreaterThanOrEqualsThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateEqualsRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateDistinctRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return !date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateGreaterThanRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return date1.isAfter(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateLessThanRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return date1.isBefore(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateGreaterThanOrEqualsThanRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return date1.isAfter(date2) || date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record date less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDateLessThanOrEqualsThanRecord(String value1, String value2) {
        try {
            LocalDate date1 = LocalDate.parse(value1, DATE_FORMAT);
            LocalDate date2 = LocalDate.parse(value2, DATE_FORMAT);
            return date1.isBefore(date2) || date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime equals.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeEquals(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() == day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime distinct.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeDistinct(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() != day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime greater than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThan(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() > day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime less than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThan(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() < day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime greater than or equals than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThanOrEqualsThan(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                   value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() >= day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime less than or equals than.
     *
     * @param value
     * @param day the day
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThanOrEqualsThan(String value, Number day) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() <= day.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() == date2.getDayOfMonth();
    }



    /**
     * Record daytime distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() != date2.getDayOfMonth();
    }



    /**
     * Record daytime greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() > date2.getDayOfMonth();
    }


    /**
     * Record daytime less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() < date2.getDayOfMonth();
    }

    /**
     * Record daytime greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() >= date2.getDayOfMonth();
    }


    /**
     * Record daytime less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getDayOfMonth() <= date2.getDayOfMonth();
    }


    /**
     * Record daytime equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() == number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() != number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() > number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() < number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() >= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record daytime less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDaytimeLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getDayOfMonth() <= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime equals.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeEquals(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() == month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime distinct.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeDistinct(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() != month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime greater than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThan(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                   value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() > month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime less than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThan(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() < month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime greater than or equals than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThanOrEqualsThan(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() >= month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime less than or equals than.
     *
     * @param value
     * @param month the month
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThanOrEqualsThan(String value, Number month) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() <= month.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() == date2.getMonthValue();
    }


    /**
     * Record monthtime distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() != date2.getMonthValue();
    }


    /**
     * Record monthtime greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() > date2.getMonthValue();
    }


    /**
     * Record monthtime less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() < date2.getMonthValue();
    }

    /**
     * Record monthtime greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() >= date2.getMonthValue();
    }


    /**
     * Record monthtime less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getMonthValue() <= date2.getMonthValue();
    }


    /**
     * Record monthtime equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                   value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() == number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() != number;
        } catch (Exception e) {
            return true;
        }
    }



    /**
     * Record monthtime greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() > number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() < number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() >= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record monthtime less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordMonthtimeLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getMonthValue() <= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime equals.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeEquals(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() == year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime distinct.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeDistinct(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() != year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime greater than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThan(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() > year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime less than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThan(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() < year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime greater than or equals than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThanOrEqualsThan(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() >= year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime less than or equals than.
     *
     * @param value
     * @param year the year
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThanOrEqualsThan(String value, Number year) {
        try {
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() <= year.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeEqualsRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() == date2.getYear();
    }


    /**
     * Record yeartime distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeDistinctRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() != date2.getYear();
    }


    /**
     * Record yeartime greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() > date2.getYear();
    }


    /**
     * Record yeartime less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() < date2.getYear();
    }


    /**
     * Record yeartime greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() >= date2.getYear();
    }


    /**
     * Record yeartime less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThanOrEqualsThanRecord(String value1, String value2) {
        LocalDate date1 = null;
        LocalDate date2 = null;
        try {
            date1 = LocalDate.parse(value1.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date1 = LocalDate.parse(value1, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        try {
            date2 = LocalDate.parse(value2.replaceAll("[zZ]$", "").replace('T', ' '),
                    DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            date2 = LocalDate.parse(value2, DATE_FORMAT);
        } catch (Exception ex) {
            return true;
        }
        return date1.getYear() <= date2.getYear();
    }


    /**
     * Record yeartime equals record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeEqualsRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() == number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime distinct record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeDistinctRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() != number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime greater than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() > number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime less than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() < number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime greater than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeGreaterThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() >= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record yeartime less than or equals than record number.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordYeartimeLessThanOrEqualsThanRecordNumber(String value1, String value2) {
        try {
            LocalDateTime date = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            int number = Integer.parseInt(value2);
            return date.getYear() <= number;
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime equals.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeEquals(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                   value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime distinct.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeDistinct(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return !fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record datetime greater than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeGreaterThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isAfter(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime less than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeLessThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isBefore(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime less than or equals than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeLessThanOrEqualsThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime greater than or equals than.
     *
     * @param value
     * @param date the date
     * @return true, if successful
     */
    public static boolean recordDatetimeGreaterThanOrEqualsThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate = LocalDateTime.parse(
                    value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime equals record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeEqualsRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime distinct record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeDistinctRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return !date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Record datetime greater than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeGreaterThanRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return date1.isAfter(date2);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime less than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeLessThanRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return date1.isBefore(date2);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime greater than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeGreaterThanOrEqualsThanRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return date1.isAfter(date2) || date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Record datetime less than or equals than record.
     *
     * @param value1
     * @param value2
     * @return true, if successful
     */
    public static boolean recordDatetimeLessThanOrEqualsThanRecord(String value1, String value2) {
        try {
            LocalDateTime date1 = LocalDateTime.parse(
                    value1.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime date2 = LocalDateTime.parse(
                    value2.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return date1.isBefore(date2) || date1.equals(date2);
        } catch (Exception e) {
            return true;
        }
    }


    // **********************************************************************
    // ******************************* FIELDS *******************************
    // **********************************************************************

    /**
     * Field and.
     *
     * @param arg1 the arg 1
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldAnd(boolean arg1, boolean arg2) {
        return arg1 && arg2;
    }

    /**
     * Field or.
     *
     * @param arg1 the arg 1
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldOr(boolean arg1, boolean arg2) {
        return arg1 || arg2;
    }

    /**
     * Field not.
     *
     * @param arg the arg
     * @return true, if successful
     */
    public static boolean fieldNot(boolean arg) {
        return !arg;
    }

    /**
     * Field null.
     *
     * @param value the value
     * @return true, if successful
     */
    public static boolean fieldNull(String value) {
        return value.isEmpty();
    }

    /**
     * Field not null.
     *
     * @param value the value
     * @return true, if successful
     */
    public static boolean fieldNotNull(String value) {
        return !value.isEmpty();
    }

    /**
     * Field number equals.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberEquals(String value, Number arg2) {
        try {
            return Double.valueOf(value).equals(arg2.doubleValue());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number distinct.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberDistinct(String value, Number arg2) {
        try {
            return !Double.valueOf(value).equals(arg2.doubleValue());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number greater than.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberGreaterThan(String value, Number arg2) {
        try {
            return Double.valueOf(value) > arg2.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number less than.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberLessThan(String value, Number arg2) {
        try {
            return Double.valueOf(value) < arg2.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number greater than or equals than.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberGreaterThanOrEqualsThan(String value, Number arg2) {
        try {
            return Double.valueOf(value) >= arg2.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number less than or equals than.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldNumberLessThanOrEqualsThan(String value, Number arg2) {
        try {
            return Double.valueOf(value) <= arg2.doubleValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field number matches.
     *
     * @param value the value
     * @param regex the regex
     * @return true, if successful
     */
    public static boolean fieldNumberMatches(String value, String regex, String countryCode) {
        try {
            return value.isEmpty() || value.matches(replaceKeywords(regex, countryCode));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field string length.
     *
     * @param value the value
     * @return the string
     */
    public static String fieldStringLength(String value) {
        try {
            return "" + value.length();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Field string equals.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldStringEquals(String value, String arg2) {
        try {
            return value.equals(arg2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field string equals ignore case.
     *
     * @param value the value
     * @param arg2 the arg 2
     * @return true, if successful
     */
    public static boolean fieldStringEqualsIgnoreCase(String value, String arg2) {
        try {
            return value.equalsIgnoreCase(arg2);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field string matches.
     *
     * @param value the value
     * @param regex the regex
     * @return true, if successful
     */
    public static boolean fieldStringMatches(String value, String regex, String countryCode) {
        try {
            return value.isEmpty() || value.matches(replaceKeywords(regex, countryCode));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayEquals(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayDistinct(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayGreaterThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayLessThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field day less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDayLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getDayOfMonth() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthEquals(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthDistinct(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthGreaterThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthLessThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field month less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getMonthValue() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearEquals(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearDistinct(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearGreaterThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearLessThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field year less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYearLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.getYear() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field date equals.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateEquals(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field date distinct.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateDistinct(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return !fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field date greater than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateGreaterThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isAfter(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }



    /**
     * Field date less than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateLessThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isBefore(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field date greater than or equals than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateGreaterThanOrEqualsThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Field date less than or equals than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDateLessThanOrEqualsThan(String value, String date) {
        try {
            LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
            return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    //
    // FIELD DATETIME
    //

    /**
     * Field daytime equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeEquals(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field daytime distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeDistinct(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field daytime greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeGreaterThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field daytime less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeLessThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field daytime greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field daytime less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldDaytimeLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getDayOfMonth() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeEquals(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeDistinct(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeGreaterThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeLessThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field monthtime less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldMonthtimeLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getMonthValue() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime equals.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeEquals(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() == number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime distinct.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeDistinct(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() != number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime greater than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeGreaterThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() > number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime less than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeLessThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() < number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime greater than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeGreaterThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() >= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field yeartime less than or equals than.
     *
     * @param value the value
     * @param number the number
     * @return true, if successful
     */
    public static boolean fieldYeartimeLessThanOrEqualsThan(String value, Number number) {
        try {
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.getYear() <= number.longValue();
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field datetime equals.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeEquals(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field datetime distinct.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeDistinct(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return !fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }



    /**
     * Field datetime greater than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeGreaterThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isAfter(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field datetime less than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeLessThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isBefore(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field datetime greater than or equals than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeGreaterThanOrEqualsThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }


    /**
     * Field datetime less than or equals than.
     *
     * @param value the value
     * @param date the date
     * @return true, if successful
     */
    public static boolean fieldDatetimeLessThanOrEqualsThan(String value, String date) {
        try {
            LocalDateTime ruleDate =
                    LocalDateTime.parse(date.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            LocalDateTime fieldDate =
                    LocalDateTime.parse(value.replaceAll("[zZ]$", "").replace('T', ' '), DATETIME_FORMAT);
            return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
        } catch (Exception e) {
            return true;
        }
    }

    private static String replaceKeywords(String regex, String countryCode) {
        if (regex.contains("{%R3_COUNTRY_CODE%}")) {
            regex = regex.replace("{%R3_COUNTRY_CODE%}", countryCode);
        }
        if (regex.contains("{%R3_COMPANY_CODE%}")) {
            regex = regex.replace("{%R3_COMPANY_CODE%}", countryCode);
        }
        if (regex.contains("{%R3_ORGANIZATION_CODE%}")) {
            regex = regex.replace("{%R3_ORGANIZATION_CODE%}", countryCode);
        }
        return regex;
    }
}
