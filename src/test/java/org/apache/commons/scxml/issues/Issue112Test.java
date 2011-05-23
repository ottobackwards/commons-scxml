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
package org.apache.commons.scxml.issues;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.CustomAction;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;

/**
 * Test cases for issue 112.
 * OPEN
 */
public class Issue112Test extends TestCase {

    public Issue112Test(String testName) {
        super(testName);
    }

    private URL queue01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        queue01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/issues/queue-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        queue01 = null;
        exec = null;
        Application.QUEUE.clear();
    }

    // Example using a custom <my:enqueue> action that generates more events during event processing.
    // An external event queue is used by <my:enqueue> instead of SCXMLExecutor#triggerEvent(TriggerEvent)
    public void test01issue112() throws Exception {

        CustomAction ca1 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "enqueue", Enqueue.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca1);

        SCXML scxml = SCXMLTestHelper.parse(queue01, customActions);

        exec = SCXMLTestHelper.getExecutor(scxml);
        assertEquals("init", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());

        // Add an event, other external events could be added to the queue at any time (this test only adds one).
        Application.QUEUE.add("next");

        // Rest of the events in this test are added by custom action invocation during processing of the one above.
        // Same concept applies to adding events in listeners, invokes and WRT AbstractStateMachine, state handlers.
        while (!Application.QUEUE.isEmpty()) {
            try {
                SCXMLTestHelper.fireEvent(exec, Application.QUEUE.remove());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertTrue(exec.getCurrentStatus().isFinal());
        assertEquals("end", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());

    }

    /**
     * A custom action that generates external events.
     */
    public static class Enqueue extends Action {

        private static final long serialVersionUID = 1L;
        private String event;

        public Enqueue() {
            super();
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        @Override
        public void execute(EventDispatcher evtDispatcher,
                ErrorReporter errRep, SCInstance scInstance, Log appLog,
                Collection<TriggerEvent> derivedEvents)
        throws ModelException, SCXMLExpressionException {

            Application.QUEUE.add(event);

        }

    }

    // Test external event queue
    private static final class Application {
        private static final Queue<String> QUEUE = new LinkedList<String>();
    }

}

