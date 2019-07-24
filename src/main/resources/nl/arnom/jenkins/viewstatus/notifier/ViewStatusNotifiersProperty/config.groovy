package nl.arnom.jenkins.viewstatus.notifier.ViewStatusNotifiersProperty

import lib.FormTagLib


def f = namespace(FormTagLib)
f.section(title: _('View Status Notifications')) {
	f.entry(title: _("Notifiers")) {
		f.repeatableHeteroProperty(
				field: "notifiers",
				hasHeader: "true",
				addCaption: _("Add notifier")
		)
	}
}