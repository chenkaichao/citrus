package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.BasicConstant.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Test;

public class ActionEventUtilTests {
    private HttpServletRequest request;
    private Capture<String> cap = new Capture<String>();

    @Test
    public void getEventName_null() {
        assertEventName(null);
    }

    @Test
    public void getEventName_submit() {
        assertEventName("update", "eventSubmitDoUpdate", "yes");

        // no value
        assertEventName(null, "eventSubmitDoUpdate", "");
        assertEventName(null, "eventSubmitDoUpdate", "  ");
    }

    @Test
    public void getEventName_imageButton() {
        assertEventName("update", "eventSubmitDoUpdate.x", "yes");
        assertEventName("update", "eventSubmitDoUpdate.y", "yes");
        assertEventName("update", "eventSubmitDoUpdate.X", "yes");
        assertEventName("update", "eventSubmitDoUpdate.Y", "yes");
    }

    @Test
    public void getEventName_case() {
        assertEventName("deleteAll", "event_Submit_do_Delete_all", "yes");
        assertEventName("deleteAll", "eventSubmit_do_DeleteAll", "yes");
        assertEventName("deleteAll", "eventSubmit_doDeleteAll", "yes");
        assertEventName("deleteAll", "EVENTSubmit_DODeleteAll", "yes");

        // 不能识别的key
        assertEventName(null, "eventSubmit_dodeleteAll", "yes");
    }

    private void assertEventName(String event, String... values) {
        initRequest(true, values);
        assertEquals(event, ActionEventUtil.getEventName(request));

        if (event == null) {
            assertSame(NULL_PLACEHOLDER, cap.getValue());
        } else {
            assertEquals(event, cap.getValue());
        }

        verify(request);

        initRequest(false);
        assertEquals(event, ActionEventUtil.getEventName(request));
        verify(request);
    }

    private void initRequest(boolean set, String... values) {
        request = createMock(HttpServletRequest.class);

        if (set) {
            cap.setValue(null);

            final Vector<String> keys = new Vector<String>();

            if (values != null) {
                for (int i = 0; i < values.length; i += 2) {
                    String key = values[i];
                    String value = values[i + 1];

                    keys.add(key);
                    expect(request.getParameter(key)).andReturn(value).anyTimes();
                }
            }

            expect(request.getParameterNames()).andAnswer(new IAnswer<Enumeration<?>>() {
                public Enumeration<?> answer() throws Throwable {
                    return keys.elements();
                }
            }).once();

            request.setAttribute(eq(ActionEventUtil.ACTION_EVENT_KEY), capture(cap));
            expectLastCall().once();
        }

        expect(request.getAttribute(ActionEventUtil.ACTION_EVENT_KEY)).andReturn(cap.getValue()).once();

        replay(request);
    }
}
