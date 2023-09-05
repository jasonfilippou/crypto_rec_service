package com.xm.cryptorecservice.util.exceptions;

import lombok.Getter;

/**
 * @ {@link RuntimeException} thrown whenever the user supplies a crypto with a name that we do not (yet)
 * support.
 */
@Getter
public class UnsupportedCryptoException extends RuntimeException{

    private final String badCryptoName;

    public UnsupportedCryptoException(String badCryptoName){
        super("Crypto " + badCryptoName + " not currently supported.");
        this.badCryptoName = badCryptoName;
    }
}
