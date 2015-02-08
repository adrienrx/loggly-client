/**
 * Copyright (C) 2015 Anthony K. Trinh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tony19;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;

/**
 * Loggly client
 *
 * @author tony19@gmail.com
 */
public class LogglyClient implements ILogglyClient {
    private static final String API_URL = "http://logs-01.loggly.com/";
    private final ILogglyRestService loggly;
    private final String token;

    /**
     * Callback for asynchronous logging
     */
    public static interface Callback {
        void success();
        void failure(String error);
    }

    /**
     * Creates a Loggly client
     * @param token Loggly customer token
     *              http://loggly.com/docs/customer-token-authentication-token/
     */
    public LogglyClient(@NotNull String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be empty");
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();

        this.token = token;
        this.loggly = restAdapter.create(ILogglyRestService.class);
    }

    /**
     * Creates a Loggly client with the specified REST API.
     * This is package private for internal testing only.
     * @param token Loggly customer token
     * @param restApi implementation of {@link ILogglyRestService}
     */
    LogglyClient(@NotNull String token, ILogglyRestService restApi) {
        this.token = token;
        this.loggly = restApi;
    }

    /**
     * Posts a log message to Loggly
     * @param message message to be logged
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean log(@NotNull String message) {
        if (message == null) return false;

        boolean ok;
        try {
            ok = loggly.log(token, new TypedString(message)).isOk();
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    /**
     * Posts a log message asynchronously to Loggly
     * @param message message to be logged
     * @param callback callback to be invoked on completion of the post
     */
    public void log(@NotNull String message, final Callback callback) {
        if (message == null) return;

        loggly.log(token,
                new TypedString(message),
                new retrofit.Callback<LogglyResponse>() {
                    public void success(LogglyResponse logglyResponse, Response response) {
                        callback.success();
                    }

                    public void failure(RetrofitError retrofitError) {
                        callback.failure(retrofitError.getMessage());
                    }
                });
    }

    /**
     * Posts several log messages in bulk to Loggly
     * @param messages messages to be logged
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean logBulk(@NotNull String... messages) {
        if (messages == null) return false;
        return logBulk(Arrays.asList(messages));
    }

    /**
     * Posts several log messages in bulk to Loggly
     * @param messages messages to be logged
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean logBulk(@NotNull Collection<String> messages) {
        if (messages == null) return false;

        String parcel = joinStrings(messages);

        boolean ok;
        try {
            ok = loggly.logBulk(token, new TypedString(parcel)).isOk();
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    /**
     * Posts several log messages in bulk to Loggly asynchronously
     * @param messages messages to be logged
     * @param callback callback to be invoked on completion of the post
     */
    public void logBulk(@NotNull Collection<String> messages, final Callback callback) {
        if (messages == null) return;

        String parcel = joinStrings(messages);

        loggly.logBulk(token,
                new TypedString(parcel),
                new retrofit.Callback<LogglyResponse>() {
                    public void success(LogglyResponse logglyResponse, Response response) {
                        callback.success();
                    }

                    public void failure(RetrofitError retrofitError) {
                        callback.failure(retrofitError.getMessage());
                    }
                });
    }

    /**
     * Combines a collection of messages to be sent to Loggly.
     * In order to preserve event boundaries, the new lines in
     * each message are replaced with '\r', which get stripped
     * by Loggly.
     * @param messages messages to be combined
     * @return a single string containing all the messages
     */
    private String joinStrings(Collection<String> messages) {
        StringBuilder b = new StringBuilder();
        for (String s : messages) {
            // Preserve new-lines in this event by replacing them
            // with "\r". Otherwise, they're processed as event
            // delimiters, resulting in unintentional multiple events.
            b.append(s.replaceAll("[\r\n]", "\r")).append('\n');
        }
        return b.toString();
    }
}
