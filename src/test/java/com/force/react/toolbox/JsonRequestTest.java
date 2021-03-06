/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.react.toolbox;

import com.force.react.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class JsonRequestTest {

    @Test
    public void publicMethods() throws Exception {
        // Catch-all test to find API-breaking changes.
        assertNotNull(JsonRequest.class.getConstructor(String.class, String.class,
                Response.Listener.class, Response.ErrorListener.class));
        assertNotNull(JsonRequest.class.getConstructor(int.class, String.class, String.class,
                Response.Listener.class, Response.ErrorListener.class));

        assertNotNull(JsonArrayRequest.class.getConstructor(String.class,
                Response.Listener.class, Response.ErrorListener.class));
        assertNotNull(JsonArrayRequest.class.getConstructor(int.class, String.class, JSONArray.class,
                Response.Listener.class, Response.ErrorListener.class));

        assertNotNull(JsonObjectRequest.class.getConstructor(String.class, JSONObject.class,
                Response.Listener.class, Response.ErrorListener.class));
        assertNotNull(JsonObjectRequest.class.getConstructor(int.class, String.class,
                JSONObject.class, Response.Listener.class, Response.ErrorListener.class));
    }
}
