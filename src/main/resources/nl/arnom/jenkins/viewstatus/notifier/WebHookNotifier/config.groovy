package nl.arnom.jenkins.viewstatus.notifier.WebHookNotifier

import lib.FormTagLib

def f = namespace(FormTagLib)

f.entry(field: "url", title: _("URL")) {
	f.textbox()
}