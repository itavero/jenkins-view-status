package nl.arnom.jenkins.viewstatus.notifier;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.ViewProperty;
import hudson.model.ViewPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

@Extension
public class ViewStatusNotifiersProperty extends ViewProperty {

	private final List<Notifier> notifiers;

	public ViewStatusNotifiersProperty() {
		this.notifiers = Lists.newArrayList();
	}

	@DataBoundConstructor
	public ViewStatusNotifiersProperty(final Notifier[] notifiers) {
		this.notifiers = Lists.newArrayList(notifiers);
	}

	public Notifier[] getNotifiers() {
		return notifiers.toArray(new Notifier[0]);
	}

	@Extension
	public static class DescriptorImpl extends ViewPropertyDescriptor {
		@Nonnull
		@Override
		public String getDisplayName() {
			return "Status Change Notifications";
		}
	}
}
