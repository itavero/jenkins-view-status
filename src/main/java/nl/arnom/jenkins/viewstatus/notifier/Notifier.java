package nl.arnom.jenkins.viewstatus.notifier;

import hudson.ExtensionPoint;
import hudson.model.*;

public abstract class Notifier extends AbstractDescribableImpl<Notifier> implements ExtensionPoint {
	public Descriptor<Notifier> getDescriptor() {
		return super.getDescriptor();
	}

	public abstract void doNotify(View view, Result newResult, Result oldResult, boolean duringStartUp);
}
