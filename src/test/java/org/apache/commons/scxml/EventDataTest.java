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
package org.apache.commons.scxml;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.scxml.env.SimpleScheduler;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.TransitionTarget;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 * Testing special variable "_eventdata"
 */
public class EventDataTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public EventDataTest(String name) {
        super(name);
    }

    // Test data
    private URL eventdata01, eventdata02, eventdata03, eventdata04;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        eventdata01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-01.xml");
        eventdata02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-02.xml");
        eventdata03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-03.xml");
        eventdata04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-04.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        eventdata01 = eventdata02 = eventdata03 = eventdata04 = null;
    }

    /**
     * Test the SCXML documents, usage of "_eventdata"
     */
    public void testEventdata01Sample() throws Exception {
    	exec = SCXMLTestHelper.getExecutor(eventdata01);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state1", currentStates.iterator().next().getId());
        TriggerEvent te = new TriggerEvent("event.foo",
            TriggerEvent.SIGNAL_EVENT, new Integer(3));
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("state3", currentStates.iterator().next().getId());
        TriggerEvent[] evts = new TriggerEvent[] { te,
            new TriggerEvent("event.bar", TriggerEvent.SIGNAL_EVENT,
            new Integer(6))};
        currentStates = SCXMLTestHelper.fireEvents(exec, evts);
        assertEquals(1, currentStates.size());
        assertEquals("state6", currentStates.iterator().next().getId());
        te = new TriggerEvent("event.baz",
            TriggerEvent.SIGNAL_EVENT, new Integer(7));
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("state7", currentStates.iterator().next().getId());
    }

    public void testEventdata02Sample() throws Exception {
    	exec = SCXMLTestHelper.getExecutor(eventdata02);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state0", currentStates.iterator().next().getId());
        TriggerEvent te1 = new TriggerEvent("connection.alerting",
            TriggerEvent.SIGNAL_EVENT, "line2");
        currentStates = SCXMLTestHelper.fireEvent(exec, te1);
        assertEquals(1, currentStates.size());
        assertEquals("state2", currentStates.iterator().next().getId());
        TriggerEvent te2 = new TriggerEvent("connection.alerting",
            TriggerEvent.SIGNAL_EVENT,
            new ConnectionAlertingPayload(4));
        currentStates = SCXMLTestHelper.fireEvent(exec, te2);
        assertEquals(1, currentStates.size());
        assertEquals("state4", currentStates.iterator().next().getId());
    }

    public void testEventdata03Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(eventdata03);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", currentStates.iterator().next().getId());
        TriggerEvent te = new TriggerEvent("event.foo",
            TriggerEvent.SIGNAL_EVENT);
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("thirty", currentStates.iterator().next().getId());
    }

    public void testEventdata04Sample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(eventdata04);
        Tracer trc = new Tracer();
        exec = new SCXMLExecutor(new JexlEvaluator(), null, trc);
        assertNotNull(exec);
        exec.setEventdispatcher(new SimpleScheduler(exec));
        exec.addListener(scxml, trc);
        exec.setStateMachine(scxml);
        exec.go();
        Thread.sleep(200); // let the 100 delay lapse
    }

    public static class ConnectionAlertingPayload {
        private int line;
        public ConnectionAlertingPayload(int line) {
            this.line = line;
        }
        public void setLine(int line) {
            this.line = line;
        }
        public int getLine() {
            return line;
        }
    }
}
