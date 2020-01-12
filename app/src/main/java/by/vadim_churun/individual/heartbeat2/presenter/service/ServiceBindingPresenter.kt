package by.vadim_churun.individual.heartbeat2.presenter.service

import by.vadim_churun.individual.heartbeat2.service.MediaServiceBinder


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