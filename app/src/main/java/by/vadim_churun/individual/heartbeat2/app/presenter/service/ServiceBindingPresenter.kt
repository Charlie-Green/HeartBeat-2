package by.vadim_churun.individual.heartbeat2.app.presenter.service

import by.vadim_churun.individual.heartbeat2.app.service.MediaServiceBinder


/** MVI Presenter for [ServiceBoundUI]. **/
class ServiceBindingPresenter {
    private val binder = MediaServiceBinder()

    fun bind(ui: ServiceBoundUI) {
        binder.bind(ui.boundContext) { service ->
            ui.onConnected(service)
        }
    }

    fun unbind(ui: ServiceBoundUI) {
        binder.unbind(ui.boundContext)
    }
}