package com.iconloop.score.oracle;

import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class StdReferenceProxy {
    private Address owner;
    private Address ref;

    public StdReferenceProxy(Address _ref) {
        this.owner = Context.getCaller();
        this.ref = _ref;
    }

    @External()
    public void transferOwnership(Address newOwner) {
        Context.require(Context.getCaller().equals(this.owner), "Caller is not the owner");
        this.owner = newOwner;
    }

    @External()
    public void setRef(Address newRef) {
        Context.require(Context.getCaller().equals(this.owner), "Caller is not the owner");
        this.ref = newRef;
    }

    @External(readonly = true)
    public Address owner() {
        return this.owner;
    }

    @External(readonly = true)
    public Address ref() {
        return this.ref;
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_reference_data(String base, String quote) {
        return (Map<String, BigInteger>) Context.call(Map.class, this.ref, "getReferenceData", base, quote);
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> get_reference_data_bulk(String[] bases, String[] quotes) {
        return (List<Map<String, BigInteger>>) Context.call(List.class, this.ref, "getReferenceDataBulk", bases,
                quotes);
    }

}
