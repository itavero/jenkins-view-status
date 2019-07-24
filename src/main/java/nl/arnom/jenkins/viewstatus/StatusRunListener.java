package nl.arnom.jenkins.viewstatus;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

@Extension
public class StatusRunListener extends RunListener<Run<?, ?>> {
	private static final Logger LOGGER = Logger.getLogger(StatusRunListener.class.getName());

	@Override
	public void onFinalized(Run<?, ?> run) {
		determineViewsAndRecalculateStatus(run, true);
	}

	@Override
	public void onDeleted(Run<?, ?> run) {
		determineViewsAndRecalculateStatus(run, false);
	}

	private void determineViewsAndRecalculateStatus(Run<?, ?> run, boolean tryAppend) {
		Job job = run.getParent();
		if (!(job instanceof TopLevelItem)) {
			return;
		}

		Jenkins jenkins = Jenkins.getInstanceOrNull();
		if (jenkins == null) {
			return;
		}

		for(View view : jenkins.getViews()) {
			if (view.contains((TopLevelItem)job)) {
				LOGGER.fine("Job " + job.getFullDisplayName() + " is on view " + view.getDisplayName() +". Trigger recalculation of status...");
				if (tryAppend) {
					StatusCache.getInstance().appendResultForView(view, run.getResult());
				} else {
					StatusCache.getInstance().forceRecalculateStatusOfView(view);
				}
			}
		}
	}
}
