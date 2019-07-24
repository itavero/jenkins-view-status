package nl.arnom.jenkins.viewstatus;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.View;
import hudson.model.ViewGroup;
import hudson.model.listeners.SaveableListener;

import java.util.logging.Logger;

@Extension
public class ViewChangeListener extends SaveableListener {
	private static final Logger LOGGER = Logger.getLogger(ViewChangeListener.class.getName());

	@Override
	public void onChange(Saveable o, XmlFile file) {
		// TODO Figure out how to use this trigger to recalculate view results

		if (o instanceof View) {
			LOGGER.finest("View saved: " + ((View) o).getDisplayName());
		}
		if (o instanceof ViewGroup) {
			LOGGER.finest("ViewGroup saved: " + ((ViewGroup) o).getDisplayName());
		}
	}
}
