package ut.com.projmodel.plugin;

import org.junit.Test;
import com.projmodel.plugin.api.MyPluginComponent;
import com.projmodel.plugin.service.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}