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
package org.apache.nifi.util;

import org.apache.nifi.flow.ComponentType;
import org.apache.nifi.flow.VersionedFlowCoordinates;
import org.apache.nifi.flow.VersionedPort;
import org.apache.nifi.flow.VersionedProcessGroup;
import org.apache.nifi.flow.VersionedProcessor;
import org.apache.nifi.flow.VersionedRemoteGroupPort;
import org.apache.nifi.registry.flow.diff.DifferenceType;
import org.apache.nifi.registry.flow.diff.StandardFlowDifference;
import org.junit.Assert;
import org.junit.Test;

public class TestFlowDifferenceFilters {

    @Test
    public void testFilterAddedRemotePortsWithRemoteInputPortAsComponentB() {
        VersionedRemoteGroupPort remoteGroupPort = new VersionedRemoteGroupPort();
        remoteGroupPort.setComponentType(ComponentType.REMOTE_INPUT_PORT);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.COMPONENT_ADDED, null, remoteGroupPort, null, null, "");

        // predicate should return false because we don't want to include changes for adding a remote input port
        Assert.assertFalse(FlowDifferenceFilters.FILTER_ADDED_REMOVED_REMOTE_PORTS.test(flowDifference));
    }

    @Test
    public void testFilterAddedRemotePortsWithRemoteInputPortAsComponentA() {
        VersionedRemoteGroupPort remoteGroupPort = new VersionedRemoteGroupPort();
        remoteGroupPort.setComponentType(ComponentType.REMOTE_INPUT_PORT);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.COMPONENT_ADDED, remoteGroupPort, null, null, null, "");

        // predicate should return false because we don't want to include changes for adding a remote input port
        Assert.assertFalse(FlowDifferenceFilters.FILTER_ADDED_REMOVED_REMOTE_PORTS.test(flowDifference));
    }

    @Test
    public void testFilterAddedRemotePortsWithRemoteOutputPort() {
        VersionedRemoteGroupPort remoteGroupPort = new VersionedRemoteGroupPort();
        remoteGroupPort.setComponentType(ComponentType.REMOTE_OUTPUT_PORT);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.COMPONENT_ADDED, null, remoteGroupPort, null, null, "");

        // predicate should return false because we don't want to include changes for adding a remote input port
        Assert.assertFalse(FlowDifferenceFilters.FILTER_ADDED_REMOVED_REMOTE_PORTS.test(flowDifference));
    }

    @Test
    public void testFilterAddedRemotePortsWithNonRemoteInputPort() {
        VersionedProcessor versionedProcessor = new VersionedProcessor();
        versionedProcessor.setComponentType(ComponentType.PROCESSOR);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.COMPONENT_ADDED, null, versionedProcessor, null, null, "");

        // predicate should return true because we do want to include changes for adding a non-port
        Assert.assertTrue(FlowDifferenceFilters.FILTER_ADDED_REMOVED_REMOTE_PORTS.test(flowDifference));
    }

    @Test
    public void testFilterIgnorableVersionedCoordinateDifferencesWithIgnorableDifference() {
        VersionedFlowCoordinates coordinatesA = new VersionedFlowCoordinates();
        coordinatesA.setRegistryUrl("http://localhost:18080");

        VersionedProcessGroup processGroupA = new VersionedProcessGroup();
        processGroupA.setVersionedFlowCoordinates(coordinatesA);

        VersionedFlowCoordinates coordinatesB = new VersionedFlowCoordinates();
        coordinatesB.setRegistryUrl("http://localhost:18080/");

        VersionedProcessGroup processGroupB = new VersionedProcessGroup();
        processGroupB.setVersionedFlowCoordinates(coordinatesB);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.VERSIONED_FLOW_COORDINATES_CHANGED,
                processGroupA, processGroupB,
                coordinatesA.getRegistryUrl(), coordinatesB.getRegistryUrl(),
                "");

        Assert.assertFalse(FlowDifferenceFilters.FILTER_IGNORABLE_VERSIONED_FLOW_COORDINATE_CHANGES.test(flowDifference));
    }

    @Test
    public void testFilterIgnorableVersionedCoordinateDifferencesWithNonIgnorableDifference() {
        VersionedFlowCoordinates coordinatesA = new VersionedFlowCoordinates();
        coordinatesA.setRegistryUrl("http://localhost:18080");

        VersionedProcessGroup processGroupA = new VersionedProcessGroup();
        processGroupA.setVersionedFlowCoordinates(coordinatesA);

        VersionedFlowCoordinates coordinatesB = new VersionedFlowCoordinates();
        coordinatesB.setRegistryUrl("http://localhost:18080");

        VersionedProcessGroup processGroupB = new VersionedProcessGroup();
        processGroupB.setVersionedFlowCoordinates(coordinatesB);

        StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.VERSIONED_FLOW_COORDINATES_CHANGED,
                processGroupA, processGroupB,
                coordinatesA.getRegistryUrl(), coordinatesB.getRegistryUrl(),
                "");

        Assert.assertTrue(FlowDifferenceFilters.FILTER_IGNORABLE_VERSIONED_FLOW_COORDINATE_CHANGES.test(flowDifference));
    }

    @Test
    public void testFilterPublicPortNameChangeWhenNotNameChange() {
        final VersionedPort portA = new VersionedPort();
        final VersionedPort portB = new VersionedPort();

        final StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.VERSIONED_FLOW_COORDINATES_CHANGED,
                portA, portB,
                "http://localhost:18080", "http://localhost:17080",
                "");

        Assert.assertTrue(FlowDifferenceFilters.FILTER_PUBLIC_PORT_NAME_CHANGES.test(flowDifference));
    }

    @Test
    public void testFilterPublicPortNameChangeWhenNotAllowRemoteAccess() {
        final VersionedPort portA = new VersionedPort();
        final VersionedPort portB = new VersionedPort();

        final StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.NAME_CHANGED,
                portA, portB,
                "Port A", "Port B",
                "");

        Assert.assertTrue(FlowDifferenceFilters.FILTER_PUBLIC_PORT_NAME_CHANGES.test(flowDifference));
    }

    @Test
    public void testFilterPublicPortNameChangeWhenAllowRemoteAccess() {
        final VersionedPort portA = new VersionedPort();
        portA.setAllowRemoteAccess(true);

        final VersionedPort portB = new VersionedPort();
        portB.setAllowRemoteAccess(false);

        final StandardFlowDifference flowDifference = new StandardFlowDifference(
                DifferenceType.NAME_CHANGED,
                portA, portB,
                "Port A", "Port B",
                "");

        Assert.assertFalse(FlowDifferenceFilters.FILTER_PUBLIC_PORT_NAME_CHANGES.test(flowDifference));
    }
}

