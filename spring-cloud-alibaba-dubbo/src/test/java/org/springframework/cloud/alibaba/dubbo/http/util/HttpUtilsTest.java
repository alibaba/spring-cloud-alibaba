/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.http.util;

import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;

import com.diffblue.deeptestutils.mock.DTUMemberMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.expectation.PowerMockitoStubber;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * {@link HttpUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@RunWith(PowerMockRunner.class)
public class HttpUtilsTest {

    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Rule public final Timeout globalTimeout = new Timeout(10000);

    /* testedClasses: HttpUtils */
    // Test written by Diffblue Cover.

    @Test
    public void testEncodeAndDecode() {

        String whitespace = " ";

        String encodedValue = HttpUtils.encode(" ");

        String decodedValue = HttpUtils.decode(encodedValue);

        Assert.assertEquals(whitespace, decodedValue);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest(HttpUtils.class)
    @Test
    public void getParametersInput0OutputNotNull() throws Exception {

        // Arrange
        final String[] pairs = {};
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(pairs);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({MultiValueMap.class, HttpUtils.class, StringUtils.class})
    @Test
    public void getParametersInput1OutputNotNull5() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final ArrayList<String> pairs = new ArrayList<String>();
        pairs.add(null);
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);
        final Method trimAllWhitespaceMethod =
            DTUMemberMatcher.method(StringUtils.class, "trimAllWhitespace", String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn(null).doReturn(null))
            .when(StringUtils.class, trimAllWhitespaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));
        final String[] stringArray = {null, null};
        final Method delimitedListToStringArrayMethod = DTUMemberMatcher.method(
            StringUtils.class, "delimitedListToStringArray", String.class, String.class);
        PowerMockito.doReturn(stringArray)
            .when(StringUtils.class, delimitedListToStringArrayMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(false)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(pairs);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({HttpUtils.class, StringUtils.class})
    @Test
    public void getParametersInputNotNullOutputNotNull() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String queryString = "Bar";
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);
        final String[] stringArray = {};
        final Method delimitedListToStringArrayMethod = DTUMemberMatcher.method(
            StringUtils.class, "delimitedListToStringArray", String.class, String.class);
        PowerMockito.doReturn(stringArray)
            .when(StringUtils.class, delimitedListToStringArrayMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(queryString);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({MultiValueMap.class, HttpUtils.class, StringUtils.class})
    @Test
    public void getParametersInputNotNullOutputNotNull2() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String queryString = "foo";
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);
        final Method trimAllWhitespaceMethod =
            DTUMemberMatcher.method(StringUtils.class, "trimAllWhitespace", String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn("1234").doReturn(","))
            .when(StringUtils.class, trimAllWhitespaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));
        final String[] stringArray1 = {null};
        final String[] stringArray = {null};
        final Method delimitedListToStringArrayMethod = DTUMemberMatcher.method(
            StringUtils.class, "delimitedListToStringArray", String.class, String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn(stringArray).doReturn(stringArray1))
            .when(StringUtils.class, delimitedListToStringArrayMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(true)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(queryString);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({MultiValueMap.class, HttpUtils.class, StringUtils.class})
    @Test
    public void getParametersInputNotNullOutputNotNull3() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String queryString = "foo";
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);
        final Method trimAllWhitespaceMethod =
            DTUMemberMatcher.method(StringUtils.class, "trimAllWhitespace", String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn("1234").doReturn(","))
            .when(StringUtils.class, trimAllWhitespaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));
        final String[] stringArray1 = {null};
        final String[] stringArray = {null};
        final Method delimitedListToStringArrayMethod = DTUMemberMatcher.method(
            StringUtils.class, "delimitedListToStringArray", String.class, String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn(stringArray).doReturn(stringArray1))
            .when(StringUtils.class, delimitedListToStringArrayMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(false)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(queryString);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest(HttpUtils.class)
    @Test
    public void getParametersInputNullOutputNotNull() throws Exception {

        // Arrange
        final Iterable pairs = null;
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(pairs);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({MultiValueMap.class, HttpUtils.class, StringUtils.class})
    @Test
    public void getParametersInputNullOutputNotNull2() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String queryString = null;
        final LinkedMultiValueMap linkedMultiValueMap = PowerMockito.mock(LinkedMultiValueMap.class);
        PowerMockito.whenNew(LinkedMultiValueMap.class)
            .withNoArguments()
            .thenReturn(linkedMultiValueMap);
        final Method trimAllWhitespaceMethod =
            DTUMemberMatcher.method(StringUtils.class, "trimAllWhitespace", String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn(null).doReturn(null))
            .when(StringUtils.class, trimAllWhitespaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));
        final String[] stringArray1 = {null, null};
        final String[] stringArray = {null};
        final Method delimitedListToStringArrayMethod = DTUMemberMatcher.method(
            StringUtils.class, "delimitedListToStringArray", String.class, String.class);
        ((PowerMockitoStubber)PowerMockito.doReturn(stringArray).doReturn(stringArray1))
            .when(StringUtils.class, delimitedListToStringArrayMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(true)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final MultiValueMap<String, String> actual = HttpUtils.getParameters(queryString);

        // Assert result
        Assert.assertNotNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest(StringUtils.class)
    @Test
    public void normalizePathInputNotNullOutputNotNull() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String path = "\'";
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(false)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final String actual = HttpUtils.normalizePath(path);

        // Assert result
        Assert.assertEquals("\'", actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest(StringUtils.class)
    @Test
    public void normalizePathInputNotNullOutputNull() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String path = "\'";
        final Method replaceMethod = DTUMemberMatcher.method(StringUtils.class, "replace", String.class,
            String.class, String.class);
        PowerMockito.doReturn(null)
            .when(StringUtils.class, replaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(true)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final String actual = HttpUtils.normalizePath(path);

        // Assert result
        Assert.assertNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest(StringUtils.class)
    @Test
    public void normalizePathInputNotNullOutputNull2() throws Exception {

        // Setup mocks
        PowerMockito.mockStatic(StringUtils.class);

        // Arrange
        final String path = "?";
        final Method replaceMethod = DTUMemberMatcher.method(StringUtils.class, "replace", String.class,
            String.class, String.class);
        PowerMockito.doReturn(null)
            .when(StringUtils.class, replaceMethod)
            .withArguments(or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)),
                or(isA(String.class), isNull(String.class)));
        final Method hasTextMethod =
            DTUMemberMatcher.method(StringUtils.class, "hasText", String.class);
        PowerMockito.doReturn(true)
            .when(StringUtils.class, hasTextMethod)
            .withArguments(or(isA(String.class), isNull(String.class)));

        // Act
        final String actual = HttpUtils.normalizePath(path);

        // Assert result
        Assert.assertNull(actual);
    }

    // Test written by Diffblue Cover.
    @PrepareForTest({HttpUtils.class, AbstractCollection.class})
    @Test
    public void toNameAndValuesInput0Output0() throws Exception {

        // Arrange
        final HashMap<String, List<String>> nameAndValuesMap = new HashMap<String, List<String>>();
        final LinkedHashSet linkedHashSet = new LinkedHashSet();
        PowerMockito.whenNew(LinkedHashSet.class).withNoArguments().thenReturn(linkedHashSet);

        // Act
        final String[] actual = HttpUtils.toNameAndValues(nameAndValuesMap);

        // Assert result
        Assert.assertArrayEquals(new String[] {}, actual);
    }
}
