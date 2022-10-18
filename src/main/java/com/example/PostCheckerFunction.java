package com.example;

import com.example.PostCheckerFunction.PubSubMessage;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Cloud Function consuming Pub/Sub events
 */
public class PostCheckerFunction implements BackgroundFunction<PubSubMessage> {

    private static final Logger logger = Logger.getLogger(PostCheckerFunction.class.getName());
    private static final String KIND_POST = "post";
    private static final String PROPERTY_TEXT = "text";
    private static final Map<String, String> REPLACEMENTS = new HashMap<>();

    static {
        REPLACEMENTS.put("toBeReplaced", "<edited>");
        REPLACEMENTS.put("rooster", "****");
    }

    @Override
    public void accept(PubSubMessage message, Context context) {
        var postId = new String(Base64.getDecoder().decode(message.data));

        var datastore = DatastoreOptions.getDefaultInstance().getService();
        var taskKey = datastore.newKeyFactory().setKind(KIND_POST).newKey(Long.parseLong(postId));
        var post = datastore.get(taskKey);
        var postText = post.getString(PROPERTY_TEXT);
        logger.info("Found new post: " + postText);

        var updatedText = postText.toLowerCase();
        Set<Map.Entry<String, String>> entries = REPLACEMENTS.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            updatedText = updatedText.replaceAll(entry.getKey().toLowerCase(), entry.getValue());
        }

        var updatedEntity = Entity.newBuilder(post)
                .set(PROPERTY_TEXT, updatedText)
                .build();

        datastore.update(updatedEntity);

        logger.info("Updated post text: " + updatedText);
    }

    public static class PubSubMessage {
        String data;
        Map<String, String> attributes;
        String messageId;
        String publishTime;
    }
}
