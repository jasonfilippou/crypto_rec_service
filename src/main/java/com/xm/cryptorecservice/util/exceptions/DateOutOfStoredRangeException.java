package com.xm.cryptorecservice.util.exceptions;

import lombok.Getter;

/**
 * A {@link RuntimeException} thrown by our code when the user provides a day to search crypto stats for
 * which is out of the range of dates that we have stored information for.
 *
 * @author jason
 */
@Getter
public class DateOutOfStoredRangeException extends RuntimeException{

    private final String date;

    public DateOutOfStoredRangeException(String date){
        super("Day " + date + " out of stored range of dates for all cryptos");
        this.date = date;
    }
}
