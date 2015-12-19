package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.enums.TextAlignment;

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
        gson = new Gson();
    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testJson2viewFormatIsOK() {

        JsonObject jsonObjectShould = new JsonObject();
        jsonObjectShould.addProperty("widget", "TextView");
        jsonObjectShould.add("properties", new JsonArray());

        ContentDto resource = new ContentDto();

        resource.setType(FeedbackItemType.TEXT.getValue());
        resource.setCaption("test");
        resource.setStyle(new String[]{});
        resource.setHighlighted(Boolean.FALSE);
        resource.setAlignment(TextAlignment.LEFT.getValue());

        JsonParser gsonParser = new JsonParser();
        JsonObject gsonJsonObject = gsonParser.parse(gson.toJson(resource)).getAsJsonObject();

        JsonObject json2ViewFormat = JsonUtils.mapFeedbackToJson2ViewFormat(gsonJsonObject);

        assertNotNull(json2ViewFormat.get("widget"));
        assertThat(jsonObjectShould.get("widget").getAsString(), is(json2ViewFormat.get("widget").getAsString()));
    }
}