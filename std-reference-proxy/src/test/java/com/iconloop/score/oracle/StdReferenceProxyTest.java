/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.oracle;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.test.ServiceManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

class StdReferenceProxyTest extends TestBase {
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount(100);
    private static Score proxyScore;

    private static BigInteger E6 = new BigInteger("1000000");
    private static BigInteger E9 = new BigInteger("1000000000");

    public static class MockRef {
        public MockRef() {
        }

        public Map<String, BigInteger> getReferenceData(String base, String quote) {
            BigInteger v = new BigInteger("0");
            if (base.equals("BTC")) {
                v = new BigInteger("16841900000000").multiply(E9);
            } else if (base.equals("ICX")) {
                v = new BigInteger("149978000").multiply(E9);
            }

            return Map.of(
                    "rate",
                    v,
                    "last_update_base",
                    new BigInteger("1671780359").multiply(E6),
                    "last_update_quote",
                    new BigInteger("1671780359").multiply(E6));
        }

        public List<Map<String, BigInteger>> getReferenceDataBulk(String[] bases, String[] quotes) {
            Map<String, BigInteger>[] results = new Map[bases.length];
            for (int i = 0; i < bases.length; i++) {
                results[i] = getReferenceData(bases[i], quotes[i]);
            }
            return List.of(results);
        }
    }

    @BeforeAll
    public static void setup() throws Exception {
        // setup owner and deploy

        var mockScore = sm.deploy(owner, MockRef.class);
        proxyScore = sm.deploy(owner, StdReferenceProxy.class, mockScore.getAddress());
    }

    // @Test
    // void testParse() {
    // String[] results = (String[]) proxyScore.call("parse", "[\"BTC\", \"ICX\"]");
    // assertEquals("BTC", results[0]);
    // assertEquals("ICX", results[1]);
    // }

    @Test
    void test_get_reference_data() {
        BigInteger rate = new BigInteger("16841900000000");
        BigInteger resolveTime = BigInteger.valueOf(1671780359);

        var result = (Map<String, BigInteger>) (proxyScore.call("get_reference_data", "BTC", "USD"));

        assertEquals(rate.multiply(E9), result.get("rate"));
        assertEquals(resolveTime.multiply(E6), result.get("last_update_base"));
    }

    @Test
    void test_get_reference_data_bulk() {
        BigInteger[] rates = { new BigInteger("16841900000000"), new BigInteger("149978000") };
        BigInteger resolveTime = BigInteger.valueOf(1671780359);

        var results = (List<Map<String, BigInteger>>) (proxyScore.call("get_reference_data_bulk", "[\"BTC\", \"ICX\"]",
                "[\"USD\", \"USD\"]"));

        for (int i = 0; i < 2; i++) {
            assertEquals(rates[i].multiply(E9), results.get(i).get("rate"));
            assertEquals(resolveTime.multiply(E6), results.get(i).get("last_update_base"));
        }
    }
}
