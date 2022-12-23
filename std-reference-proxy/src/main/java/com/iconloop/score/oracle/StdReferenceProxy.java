package com.iconloop.score.oracle;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.External;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class StdReferenceProxy {
    private final VarDB<Address> owner = Context.newVarDB("owner", Address.class);
    private final VarDB<Address> ref = Context.newVarDB("ref", Address.class);

    public StdReferenceProxy(Address _ref) {
        if (this.owner.get() == null) {
            this.owner.set(Context.getCaller());
            this.ref.set(_ref);
        }
    }

    @External()
    public void transferOwnership(Address newOwner) {
        Context.require(Context.getCaller().equals(this.owner.get()), "Caller is not the owner");
        this.owner.set(newOwner);
    }

    @External()
    public void setRef(Address newRef) {
        Context.require(Context.getCaller().equals(this.owner.get()), "Caller is not the owner");
        this.ref.set(newRef);
    }

    @External(readonly = true)
    public Address owner() {
        return this.owner.get();
    }

    @External(readonly = true)
    public Address ref() {
        return this.ref.get();
    }

    @External(readonly = true)
    public Map<String, BigInteger> getReferenceData(String base, String quote) {
        return (Map<String, BigInteger>) Context.call(Map.class, this.ref.get(), "getReferenceData", base, quote);
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_reference_data(String _base, String _quote) {
        return getReferenceData(_base, _quote);
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> getReferenceDataBulk(String[] bases, String[] quotes) {
        return (List<Map<String, BigInteger>>) Context.call(List.class, this.ref.get(), "getReferenceDataBulk", bases,
                quotes);
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> get_reference_data_bulk(String _bases, String _quotes) {
        String[] bases = _bases.split("\\s*,\\s*");
        String[] quotes = _quotes.split("\\s*,\\s*");
        return getReferenceDataBulk(bases, quotes);
    }
}
