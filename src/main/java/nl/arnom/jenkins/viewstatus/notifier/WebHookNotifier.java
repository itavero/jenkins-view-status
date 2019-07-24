package nl.arnom.jenkins.viewstatus.notifier;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.View;
import hudson.util.FormValidation;
import jenkins.org.apache.commons.validator.routines.UrlValidator;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class WebHookNotifier extends Notifier {
	private static final Logger LOGGER = Logger.getLogger(WebHookNotifier.class.getName());

	private final String url;

	public WebHookNotifier() {
		url = "";
	}

	@DataBoundConstructor
	public WebHookNotifier(String url) {
		this.url = url;
	}

	public String getUrl() { return url; }

	@Override
	public void doNotify(View view, Result newResult, Result oldResult, boolean duringStartUp) {
		JSONObject postData = convertPostData(view, newResult, oldResult, duringStartUp);
		try {
			HttpResponse response = Request.Post(url).bodyString(postData.toString(), ContentType.APPLICATION_JSON).execute().returnResponse();
			if (response.getStatusLine().getStatusCode() < 400) {
				LOGGER.finest("Web Hook call succeeded for view " + view.getViewName() + " to " + url);
			} else {
				LOGGER.warning("Web Hook call for view " + view.getViewName() + " to '" + url +"' resulted in status " + response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			LOGGER.warning("Web Hook call resulted in an exception (" + url + "): " + e.getMessage());
		}
	}

	private static JSONObject convertPostData(View view, Result newResult, Result oldResult, boolean duringStartUp) {
		JSONObject data = new JSONObject();
		data.put("view", convertViewToJson(view));
		data.put("result", convertResultToJson(newResult));
		data.put("old_result", convertResultToJson(oldResult));
		data.put("startup", duringStartUp);
		return data;
	}

	private static JSONObject convertViewToJson(View view) {
		JSONObject viewData = new JSONObject();
		viewData.put("name", view.getViewName());
		viewData.put("display_name", view.getDisplayName());
		viewData.put("description", view.getDescription());
		viewData.put("url", view.getAbsoluteUrl());
		return viewData;
	}

	private static  JSONObject convertResultToJson(Result result) {
		if (result == null) {
			return null;
		}

		JSONObject resultData = new JSONObject();
		resultData.put("text", result.toString());
		resultData.put("numeric", result.ordinal);
		return resultData;
	}

	@Extension
	@Symbol("webhook")
	public static class DescriptorImpl extends Descriptor<Notifier> {
		@Nonnull
		public String getDisplayName() {
			return "Web Hook";
		}

		public FormValidation doCheckUrl(@QueryParameter String value) {
			UrlValidator validator = new UrlValidator(new String[] {"http","https"});
			if (!validator.isValid(value)) {
				return FormValidation.error("URL must be a valid HTTP or HTTPS endpoint.");
			}
			return FormValidation.ok();
		}
	}
}
