package de.tudarmstadt.informatik.tk.android.assistance.handler;

import java.util.List;

import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 16.12.2015
 */
public interface OnModuleFeedbackResponseHandler {

    void onModuleFeedbackSuccess(List<ClientFeedbackDto> clientFeedbackDto, Response response);

    void onModuleFeedbackFailed(RetrofitError error);
}