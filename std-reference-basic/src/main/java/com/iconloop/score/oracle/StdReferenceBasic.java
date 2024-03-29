package com.iconloop.score.oracle;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.External;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class StdReferenceBasic {
    private final VarDB<Address> owner = Context.newVarDB("owner", Address.class);
    private final BigInteger E9 = new BigInteger("1000000000");
    private final DictDB<Address, Boolean> isRelayer = Context.newDictDB("isRelayer", Boolean.class);
    private final DictDB<String, BigInteger> rates = Context.newDictDB("rates", BigInteger.class);
    private final DictDB<String, BigInteger> resolveTimes = Context.newDictDB("resolveTimes", BigInteger.class);
    private final DictDB<String, BigInteger> requestIDs = Context.newDictDB("requestIDs", BigInteger.class);

    public StdReferenceBasic() {
        if (this.owner.get() == null) {
            this.owner.set(Context.getCaller());
            this.isRelayer.set(this.owner.get(), true);
        }
    }

    @External(readonly = true)
    public Address owner() {
        return this.owner.get();
    }

    @External(readonly = true)
    public boolean isRelayer(Address relayer) {
        return this.isRelayer.getOrDefault(relayer, false);
    }

    @External(readonly = true)
    public Map<String, BigInteger> getRefData(String symbol) {
        if (symbol.equals("USD")) {
            return Map.of(
                    "rate",
                    this.E9,
                    "last_update",
                    BigInteger.valueOf(Context.getBlockTimestamp()),
                    "request_id",
                    BigInteger.ZERO);
        }

        BigInteger rate = rates.getOrDefault(symbol, BigInteger.ZERO);
        BigInteger resolveTime = resolveTimes.getOrDefault(symbol, BigInteger.ZERO).multiply(new BigInteger("1000000"));
        BigInteger requestID = requestIDs.getOrDefault(symbol, BigInteger.ZERO);

        Context.require(resolveTime.compareTo(BigInteger.ZERO) > 0, "REFDATANOTAVAILABLE");

        return Map.of(
                "rate",
                rate,
                "last_update",
                resolveTime,
                "request_id",
                requestID);
    }

    @External(readonly = true)
    public Map<String, BigInteger> getReferenceData(String base, String quote) {
        Map<String, BigInteger> b = getRefData(base);
        Map<String, BigInteger> q = getRefData(quote);

        return Map.of(
                "rate",
                b.get("rate").multiply(this.E9.multiply(this.E9)).divide(q.get("rate")),
                "last_update_base",
                b.get("last_update"),
                "last_update_quote",
                q.get("last_update"));
    }

    @External(readonly = true)
    public List<Map<String, BigInteger>> getReferenceDataBulk(String[] bases, String[] quotes) {
        Context.require(bases.length == quotes.length, "Size of bases and quotes must be equal");
        Map<String, BigInteger>[] result = new Map[bases.length];
        for (int i = 0; i < bases.length; i++) {
            result[i] = getReferenceData(bases[i], quotes[i]);
        }
        return List.of(result);
    }

    @External()
    public void transferOwnership(Address newOwner) {
        Context.require(Context.getCaller().equals(this.owner.get()), "Caller is not the owner");
        this.owner.set(newOwner);
    }

    @External()
    public void addRelayer(Address relayer) {
        Context.require(Context.getCaller().equals(this.owner.get()), "Caller is not the owner");
        this.isRelayer.set(relayer, true);
    }

    @External()
    public void removeRelayer(Address relayer) {
        Context.require(Context.getCaller().equals(this.owner.get()), "Caller is not the owner");
        this.isRelayer.set(relayer, false);
    }

    @External
    public void relay(String[] symbols, BigInteger[] rates, BigInteger resolveTime, BigInteger requestID) {
        Context.require(this.isRelayer.getOrDefault(Context.getCaller(), false), "NOTARELAYER");
        Context.require(rates.length == symbols.length, "BADRATESLENGTH");

        for (int idx = 0; idx < symbols.length; idx++) {
            if (resolveTime.compareTo(this.resolveTimes.getOrDefault(symbols[idx], BigInteger.ZERO)) > 0) {
                this.rates.set(symbols[idx], rates[idx]);
                this.resolveTimes.set(symbols[idx], resolveTime);
                this.requestIDs.set(symbols[idx], requestID);
            }
        }
    }

    @External
    public void forceRelay(String[] symbols, BigInteger[] rates, BigInteger resolveTime, BigInteger requestID) {
        Context.require(this.isRelayer.getOrDefault(Context.getCaller(), false), "NOTARELAYER");
        Context.require(rates.length == symbols.length, "BADRATESLENGTH");

        for (int idx = 0; idx < symbols.length; idx++) {
            this.rates.set(symbols[idx], rates[idx]);
            this.resolveTimes.set(symbols[idx], resolveTime);
            this.requestIDs.set(symbols[idx], requestID);
        }
    }

}
