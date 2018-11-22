package org.sgitario.lottery.blockchain.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.sgitario.lottery.utils.ConvertUtils;

import lombok.Getter;

@Getter
public class Balance {

    private final BigInteger wei;
    private final BigDecimal ether;

    public Balance(BigInteger wei) {
        this.wei = wei;
        this.ether = ConvertUtils.toEther(wei);
    }
}
