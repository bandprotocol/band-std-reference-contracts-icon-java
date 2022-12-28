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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Map;

class StdReferenceBasicTest extends TestBase {
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount(100);
    private static Account[] relayers;
    private static Score refScore;

    private BigInteger E6 = new BigInteger("1000000");
    private BigInteger E9 = new BigInteger("1000000000");

    @BeforeAll
    public static void setup() throws Exception {
        // setup owner and deploy
        refScore = sm.deploy(owner, StdReferenceBasic.class);

        // register relayers
        relayers = new Account[3];
        for (int i = 0; i < relayers.length; i++) {
            relayers[i] = sm.createAccount(100);
            refScore.invoke(owner, "addRelayer", relayers[i].getAddress());
        }
    }

    @Test
    void relay() {
        String[] symbols = { "BTC", "ICX" };
        BigInteger[] rates = { new BigInteger("16841900000000"), new BigInteger("149978000") };
        BigInteger resolveTime = BigInteger.valueOf(1671780359);
        BigInteger requestID = BigInteger.valueOf(10323);
        refScore.invoke(relayers[0], "relay", symbols, rates, resolveTime, requestID);

        var result = (Map<String, BigInteger>) (refScore.call("getReferenceData", "BTC", "USD"));

        assertEquals(rates[0].multiply(E9), result.get("rate"));
        assertEquals(resolveTime.multiply(E6), result.get("last_update_base"));
    }

    @Test
    void relayByOtherAccount() {
        String[] symbols = { "BTC", "ICX" };
        BigInteger[] rates = { new BigInteger("16841900000000"), new BigInteger("149978000") };
        BigInteger resolveTime = BigInteger.valueOf(1671780359);
        BigInteger requestID = BigInteger.valueOf(10323);
        assertThrows(AssertionError.class,
                () -> refScore.invoke(sm.createAccount(10), "relay", symbols, rates, resolveTime, requestID));
    }
}
