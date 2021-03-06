package reactor.spring.core.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Timers;
import reactor.core.processor.ExecutorProcessor;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.core.support.wait.BlockingWaitStrategy;
import reactor.core.support.wait.WaitStrategy;
import reactor.core.timer.Timer;

import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Implementation of an {@link org.springframework.core.task.AsyncTaskExecutor} that is backed by a Reactor {@link
 * RingBufferWorkProcessor}.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 * @since 1.1, 2.1
 */
public class WorkQueueAsyncTaskExecutor extends AbstractAsyncTaskExecutor implements ApplicationEventPublisherAware {

	private final Logger log = LoggerFactory.getLogger(WorkQueueAsyncTaskExecutor.class);

	private WaitStrategy                      waitStrategy;
	private ExecutorProcessor<Runnable, Runnable> workQueue;

	public WorkQueueAsyncTaskExecutor() {
		this(Timers.globalOrNew());
	}

	public WorkQueueAsyncTaskExecutor(Timer timer) {
		super(timer);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!isShared()) {
			this.workQueue = RingBufferWorkProcessor.create(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new BlockingWaitStrategy())
			);
		} else {
			this.workQueue = RingBufferWorkProcessor.share(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new BlockingWaitStrategy())
			);
		}
		if (isAutoStartup()) {
			start();
		}
	}

	/**
	 * Get the {@link reactor.core.support.wait.WaitStrategy} this {@link reactor.core.support.rb.disruptor.RingBuffer} is using.
	 *
	 * @return the {@link reactor.core.support.wait.WaitStrategy}
	 */
	public WaitStrategy getWaitStrategy() {
		return waitStrategy;
	}

	/**
	 * Set the {@link reactor.core.support.wait.WaitStrategy} to use when creating the internal {@link
	 * reactor.core.support.rb.disruptor.RingBuffer}.
	 *
	 * @param waitStrategy
	 * 		the {@link reactor.core.support.wait.WaitStrategy}
	 */
	public void setWaitStrategy(WaitStrategy waitStrategy) {
		this.waitStrategy = waitStrategy;
	}

	@Override
	protected ExecutorProcessor<Runnable, Runnable> getProcessor() {
		return workQueue;
	}

}
