# Band Protocol's Java Standard Reference Contracts

## Overview

This repository contains the Java code for Band Protocol's StdReference contracts. The live contract
addresses can be found in
our [documentation](https://docs.bandchain.org/band-standard-dataset/supported-blockchains.html).

## Build

### Contract

To compile all contracts, run the following script in the repo root.

```console
$ ./gradlew build
$ ./gradlew optimizedJar
```

The optimized jar files can be found in `std-reference-basic/build/libs/` and `std-reference-proxy/build/libs/` directory.

## Usage

To query the prices from Band Protocol's StdReference contracts, the contract looking to use the price values should
query Band Protocol's `std_reference_proxy` contract.

### QueryMsg

Acceptable query functions for the `std_reference_proxy` contract are as follows:

```java
public Map<String, BigInteger> get_reference_data(String base, String quote) {
        return (Map<String, BigInteger>) Context.call(Map.class, this.ref, "getReferenceData", base, quote);
}

public List<Map<String, BigInteger>> get_reference_data_bulk(String[] bases, String[] quotes) {
        return (List<Map<String, BigInteger>>) Context.call(List.class, this.ref, "getReferenceDataBulk", bases,
                quotes);
}
```

### Query Result

The `result` data is defined as mapping:

```java
Map.of(
        "rate", value1,
        "last_update_base", value2,
        "last_update_quote", value3
)
```

where each field:

- `rate` is defined as the base/quote exchange rate multiplied by 1e18.
- `last_update_base` is defined as the UNIX epoch of the last time the base price was updated.
- `last_update_quote` is defined as the UNIX epoch of the last time the quote price was updated.

### GetReferenceData

#### Input

- The base symbol as type `String`
- The quote symbol as type `String`

#### Output

- The base quote pair result as mapping of `ReferenceData`'s field and its value

#### Example

For example, if we wanted to query the price of `BTC/USD`, the demo function below shows how this can be done.

```java
public Map<String, BigInteger> demo(Address proxyAddr, String base, String quote) {
        return (Map<String, BigInteger>) Context.call(Map.class, proxyAddr, "get_reference_data", base, quote);
    }
```

Where the result from demo(proxy_address, "BTC", "USD") would yield as hexadecimal string value:

```json
{
  "rate": "0x4E5F2DB564771E70000",
  "last_update_base": "0x62EB4E85",
  "last_update_quote": "0x62EB5379"
}
```

and the results can be interpreted as:

- BTC/USD
  - `rate = 23131.27 BTC/USD`
  - `lastUpdatedBase = 1659588229`
  - `lastUpdatedQuote = 1659589497`

### GetReferenceDataBulk

#### Input

- A array of base symbols as type `String[]`
- A array of quote symbol as type `String[]`

#### Output

- A array of the base quote pair mapping results as type `List<Map<String, BigInteger>>`

#### Example

For example, if we wanted to query the price of `BTC/USD` and `ETH/BTC`, the demo contract below shows how this can be
done.

```java
public List<Map<String, BigInteger>> demo(Address proxyAddr, String[] bases, String[] quotes) {
        return (List<Map<String, BigInteger>>) Context.call(List.class, proxyAddr, "get_reference_data_bulk", bases,
                quotes);
    }
```

Where the result from `demo(proxy_address, ["BTC", "ETH"], ["USD", "BTC"])` would yield:

```json
[
  {
    "rate": "0x4E5F2DB564771E70000",
    "last_update_base": "0x62EB4E85",
    "last_update_quote": "0x62EB5379"
  },
  {
    "rate": "0xFE616F75EB639A",
    "last_update_base": "0x62EB4E85",
    "last_update_quote": "0x62EB5379"
  }
]
```

and the results can be interpreted as:

- BTC/USD
  - `rate = 23131.27 BTC/USD`
  - `last_update_base = 1659588229`
  - `last_update_quote = 1659589497`
- ETH/BTC
  - `rate = 0.07160177543213148 ETH/BTC`
  - `last_update_base = 1659588229`
  - `last_update_quote = 1659589497`
