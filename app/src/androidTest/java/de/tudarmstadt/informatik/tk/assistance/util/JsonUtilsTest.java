package de.tudarmstadt.informatik.tk.assistance.util;

import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.TextAlignment;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.12.2015
 */
@RunWith(AndroidJUnit4.class)
public class JsonUtilsTest {

    private Gson gson;

    public JsonUtilsTest() {
        gson = JsonUtils.getInstance().getGson();
    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testIsJsonValid() {

        ContentDto resource = new ContentDto();

        resource.setType(FeedbackItemType.TEXT.getValue());
        resource.setCaption("test");
        resource.setStyle(new String[]{});
        resource.setHighlighted(Boolean.FALSE);
        resource.setAlignment(TextAlignment.LEFT.getValue());

        assertNotNull(gson.toJson(resource));
        assertThat(true, is(JsonUtils.getInstance().isValidJSON(gson.toJson(resource))));
    }
}