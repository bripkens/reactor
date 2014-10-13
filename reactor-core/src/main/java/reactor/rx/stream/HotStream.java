/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package reactor.rx.stream;

import org.reactivestreams.Subscriber;
import reactor.core.Environment;
import reactor.event.dispatch.Dispatcher;
import reactor.rx.action.Action;
import reactor.rx.subscription.PushSubscription;

/**
 * @author Stephane Maldini
 */
public class HotStream<O> extends Action<O, O> {

	public static enum FinalState {
		ERROR,
		COMPLETE
	}

	private FinalState finalState = null;
	private Throwable error;

	public HotStream(Dispatcher dispatcher, long capacity) {
		super(dispatcher, capacity);
	}

	@Override
	protected void doNext(O ev) {
		broadcastNext(ev);
	}

	@Override
	public void broadcastError(Throwable ev) {
		this.error = ev;
		this.finalState = FinalState.ERROR;
		super.broadcastError(ev);
	}

	@Override
	public void broadcastComplete() {
		this.finalState = FinalState.COMPLETE;
		super.broadcastComplete();
	}

	@Override
	public void subscribe(Subscriber<? super O> subscriber) {
		if(finalState == null){
			if (upstreamSubscription == null){
				this.upstreamSubscription = new PushSubscription<>(null, this);
			}
			super.subscribe(subscriber);
		}else if (isComplete()){
			subscriber.onComplete();
		}else if (hasFailed()){
			subscriber.onError(error);
		}
	}

	@Override
	public HotStream<O> env(Environment environment) {
		super.env(environment);
		return this;
	}

	@Override
	public HotStream<O> capacity(long elements) {
		super.capacity(elements);
		return this;
	}

	@Override
	public HotStream<O> keepAlive(boolean keepAlive) {
		super.keepAlive(keepAlive);
		return this;
	}

	@Override
	public HotStream<O> cancel() {
		super.cancel();
		return this;
	}

	@Override
	public HotStream<O> pause() {
		super.pause();
		return this;
	}

	@Override
	public HotStream<O> resume() {
		super.resume();
		return this;
	}

	public boolean isComplete(){
		return finalState == FinalState.COMPLETE;
	}

	public boolean hasFailed(){
		return finalState == FinalState.ERROR;
	}


	public Throwable error(){
		return error;
	}
}
