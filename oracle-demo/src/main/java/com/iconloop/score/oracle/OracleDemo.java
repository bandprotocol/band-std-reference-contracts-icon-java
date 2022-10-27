package com.iconloop.score.oracle;

import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class OracleDemo {
    private Address ref;

    public OracleDemo(Address _ref) {
        this.ref = _ref;
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> demo(String[] bases, String[] quotes) {
        return (List<Map<String, BigInteger>>) Context.call(List.class, this.ref, "get_reference_data_bulk", bases,
                quotes);
    }
}
