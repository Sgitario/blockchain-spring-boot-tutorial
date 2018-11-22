package org.sgitario.lottery.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

public class ConvertUtils {
    public static BigDecimal toEther(BigInteger wei) {
        return Convert.fromWei(new BigDecimal(wei), Unit.ETHER);
    }
}
