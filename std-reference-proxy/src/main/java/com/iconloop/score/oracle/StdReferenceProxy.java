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

    private String[] _split(String s) {
        int sepCount = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ',') {
                sepCount++;
            }
        }

        String[] results = new String[sepCount + 1];
        for (int i = 0; i < sepCount; i++) {
            int idx = s.indexOf(",");
            String tmp = s.substring(0, idx).trim();
            // Remove quote
            results[i] = tmp.substring(1, tmp.length() - 1);
            // results[i] = tmp;
            // results[i] = s.substring(1, s.length() - 1);
            s = s.substring(idx + 1);
        }
        s = s.trim();
        results[sepCount] = s.substring(1, s.length() - 1);
        return results;
    }

    private String _remove_bracket(String s) {
        return s.substring(1, s.length() - 1);
    }

    public String[] _parse(String s) {
        return _split(_remove_bracket(s));
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> get_reference_data_bulk(String _bases, String _quotes) {
        String[] bases = _parse(_bases);
        String[] quotes = _parse(_quotes);
        return getReferenceDataBulk(bases, quotes);
    }
}
