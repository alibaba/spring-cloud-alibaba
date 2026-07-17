package com.alibaba.cloud.nacos.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InetIPv6Utils}.
 *
 * @author uuuyuqi
 */
class InetIPv6UtilsTests {

    private static final String EXPECTED_IPV6_ADDRESS = "[2001:db8:0:0:0:0:0:1]";

    private final InetIPv6Utils inetIPv6Utils = new InetIPv6Utils(new InetUtilsProperties());

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNetworkInterfaceStates")
    void shouldIgnoreInvalidNetworkInterface(String description, boolean up,
                                             boolean virtual, boolean loopback) throws Exception {
        NetworkInterface networkInterface = networkInterface(up, virtual, loopback);

        try (MockedStatic<NetworkInterface> networkInterfaces = mockStatic(NetworkInterface.class)) {
            networkInterfaces.when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(Collections.enumeration(List.of(networkInterface)));

            assertThat(inetIPv6Utils.findIPv6Address()).isNull();
        }
    }

    @Test
    void shouldSelectAddressFromValidNetworkInterface() throws Exception {
        NetworkInterface networkInterface = networkInterface(true, false, false);

        try (MockedStatic<NetworkInterface> networkInterfaces = mockStatic(NetworkInterface.class)) {
            networkInterfaces.when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(Collections.enumeration(List.of(networkInterface)));

            assertThat(inetIPv6Utils.findIPv6Address()).isEqualTo(EXPECTED_IPV6_ADDRESS);
        }
    }

    private static Stream<Arguments> invalidNetworkInterfaceStates() {
        return Stream.of(arguments("down interface", false, false, false),
                arguments("virtual interface", true, true, false),
                arguments("loopback interface", true, false, true));
    }

    private static NetworkInterface networkInterface(boolean up, boolean virtual,
                                                     boolean loopback) throws Exception {
        NetworkInterface networkInterface = mock(NetworkInterface.class);
        when(networkInterface.isUp()).thenReturn(up);
        when(networkInterface.isVirtual()).thenReturn(virtual);
        when(networkInterface.isLoopback()).thenReturn(loopback);
        when(networkInterface.getDisplayName()).thenReturn("test0");
        when(networkInterface.getInetAddresses())
                .thenReturn(Collections.enumeration(List.of(globalIPv6Address())));
        return networkInterface;
    }

    private static InetAddress globalIPv6Address() throws Exception {
        return InetAddress.getByAddress("test-host",
                new byte[] { 0x20, 0x01, 0x0d, (byte) 0xb8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
    }

}
