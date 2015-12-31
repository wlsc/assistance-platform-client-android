package de.tudarmstadt.informatik.tk.android.assistance.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;


/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.12.2015
 */
public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    private JsonUtils() {
    }

    /**
     * Mapping of an object received from server as feedback for modules
     * into a valid json2view library format
     *
     * @param oldObject
     * @return
     */
    public static JsonObject mapFeedbackToJson2ViewFormat(JsonObject oldObject)
            throws ClassCastException, IllegalStateException {

        if (oldObject == null) {
            return null;
        }

        JsonObject newObject = null;

        String contentTypeJson = oldObject.get("type").getAsString();
        FeedbackItemType contentType = FeedbackItemType.getEnum(contentTypeJson);

        // mapping content to the right format
        switch (contentType) {
            case BUTTON:
                newObject = formatButtonObject(oldObject);
                break;
            case TEXT:
                newObject = formatTextObject(oldObject);
                break;
            case IMAGE:
                newObject = formatImageObject(oldObject);
                break;
            default:
                newObject = null;
                break;
        }

        return newObject;
    }

    /**
     * Formats group object into json2view format
     * JSON example:
     * "caption": "test",
     * "highlighted": false,
     * "style": [],
     * "textAlignment": "LEFT",
     * "type": "text"
     *
     * @param oldObject
     * @return
     */
    private static JsonObject formatGroupObject(JsonObject oldObject) {

        JsonObject newObject = null;

        return newObject;
    }

    /**
     * Formats image object into json2view format
     * JSON example:
     * "source": "img url",
     * "target": "url/app",
     * "priority": 1,
     * "type": "image"
     *
     * @param oldObject
     * @return
     */
    private static JsonObject formatImageObject(JsonObject oldObject) {

        if (oldObject == null) {
            return null;
        }

        JsonArray properties = new JsonArray();

        JsonObject newObject = new JsonObject();
        newObject.addProperty("widget", "ImageView");
        newObject.add("properties", properties);

        return newObject;
    }

    /**
     * Formats button object into json2view format
     * JSON example:
     * "caption": "test",
     * "target": "url/app",
     * "priority": 1,
     * "type": "button"
     *
     * @param oldObject
     * @return
     */
    private static JsonObject formatButtonObject(JsonObject oldObject)
            throws ClassCastException, IllegalStateException {

        if (oldObject == null) {
            return null;
        }

        String caption = oldObject.get("caption").getAsString();

        JsonArray properties = new JsonArray();

        JsonObject captionObject = new JsonObject();
        captionObject.addProperty("name", "id");
        captionObject.addProperty("type", "string");
        captionObject.addProperty("value", caption);

        properties.add(captionObject);

        JsonObject newObject = new JsonObject();
        newObject.addProperty("widget", "Button");
        newObject.add("properties", properties);

        return newObject;
    }

    /**
     * Formats text object into json2view format
     * JSON example:
     * "caption": "test",
     * "highlighted": false,
     * "style": [],
     * "textAlignment": "LEFT",
     * "type": "text"
     *
     * @param oldObject
     * @return
     */
    private static JsonObject formatTextObject(JsonObject oldObject) {

        if (oldObject == null) {
            return null;
        }

        String caption = oldObject.get("caption").getAsString();
        boolean highlighted = oldObject.get("highlighted").getAsBoolean();
        String alignment = oldObject.get("textAlignment").getAsString();

        JsonArray properties = new JsonArray();

        JsonObject captionObject = new JsonObject();
        captionObject.addProperty("name", "id");
        captionObject.addProperty("type", "string");
        captionObject.addProperty("value", caption);

        properties.add(captionObject);

        if (highlighted) {

            JsonObject highlightedObject = new JsonObject();

            highlightedObject.addProperty("name", "textColor");
            highlightedObject.addProperty("type", "color");
            highlightedObject.addProperty("value", "0xFF0000");

            properties.add(highlightedObject);
        }

        JsonObject alignmentObject = new JsonObject();
        alignmentObject.addProperty("name", "gravity");
        alignmentObject.addProperty("type", "string");
        alignmentObject.addProperty("value", alignment.toUpperCase());

        properties.add(alignmentObject);

        JsonObject newObject = new JsonObject();
        newObject.addProperty("widget", "TextView");
        newObject.add("properties", properties);

        return newObject;
    }

    /**
     * Checks for valid JSON string with GSON library
     *
     * @param json
     * @return
     */

    public static boolean isValidJSON(String json) {

        try {

            Gson gson = new Gson();
            gson.fromJson(json, Object.class);
            return true;

        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    /**
     * Convert from org.JSON to com.google.json format
     *
     * @param object
     * @return
     */
    public static JsonElement convert(JSONObject object) {

        if (object == null) {
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(object.toString());

        return jsonElement;
    }
}