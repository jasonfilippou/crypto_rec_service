package com.xm.cryptorecservice.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AcceptedCryptoNames {

    private final Set<String> cryptoNamesSet = new HashSet<>();

    public void initialize(@NonNull List<String> cryptoNames){
        cryptoNames.parallelStream().forEach(name -> cryptoNamesSet.add(name.strip()));
    }

    public void addCryptoName(@NonNull String newCryptoName){
        cryptoNamesSet.add(newCryptoName.strip()); // Does nothing and returns false() if element already in set.
    }

    public String removeCryptoName(@NonNull String cryptoName){
        return cryptoNamesSet.remove(cryptoName) ? cryptoName : null;
    }

    public boolean containsCryptoName(String cryptoName){
        return cryptoNamesSet.contains(cryptoName);
    }

    public List<String> values() {
        return cryptoNamesSet.stream().toList();
    }
}
