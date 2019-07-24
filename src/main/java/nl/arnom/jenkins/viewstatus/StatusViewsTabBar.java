package nl.arnom.jenkins.viewstatus;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.View;
import hudson.views.ViewsTabBar;
import hudson.views.ViewsTabBarDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class StatusViewsTabBar extends ViewsTabBar {

	@DataBoundConstructor
	public StatusViewsTabBar() {
	}

	@Extension
	@Symbol("view-status")
	public static class DescriptorImpl extends ViewsTabBarDescriptor {
		@Override
		public String getDisplayName() {
			return "Tabs with statuses / results for each view";
		}
	}

	public static Result getViewResult(View view) {
		return StatusCache.getInstance().getViewResult(view);
	}
}
