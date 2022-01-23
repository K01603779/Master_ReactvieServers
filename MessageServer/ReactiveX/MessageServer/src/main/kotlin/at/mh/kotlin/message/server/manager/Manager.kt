package at.mh.kotlin.message.server.manager

import at.mh.kotlin.message.server.messages.Message
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

abstract class Manager : Observable<Message>(), Observer<Message> {
    override fun onSubscribe(d: Disposable?) {
    }

    override fun subscribeActual(manager: Observer<in Message>?) {
    }
}