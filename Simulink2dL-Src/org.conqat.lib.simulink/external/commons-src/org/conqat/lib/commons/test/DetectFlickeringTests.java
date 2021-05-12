/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2018 The ConQAT Project                               |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
|                                                                          |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Custom JUnit test rule which detects flickering tests.
 */
public class DetectFlickeringTests implements TestRule {

	/** By default, retry the test at most once. */
	private static final int DEFAULT_MAX_RETRY_COUNT = 1;

	/**
	 * Maximum number of times this rule retries the test until it finally gives up.
	 */
	private final int maxRetryCount;

	/**
	 * A snapshot of the process's environment variables.
	 */
	private final Map<String, String> environment;

	/** Constructor. */
	public DetectFlickeringTests() {
		this(DEFAULT_MAX_RETRY_COUNT);
	}

	/** Constructor. */
	public DetectFlickeringTests(int retryCount) {
		this(retryCount, System.getenv());
	}

	/** Constructor to be used only by tests. */
	/* package */ DetectFlickeringTests(int retryCount, Map<String, String> environment) {
		assertTrue(retryCount >= 0);
		this.maxRetryCount = retryCount;
		this.environment = environment;
	}

	/** {@inheritDoc} */
	@Override
	public Statement apply(Statement base, Description description) {
		return new RetryStatement(base, description);
	}

	private final class RetryStatement extends Statement {

		/**
		 * Environment variable set when the process is executed by GitLab CI.
		 * 
		 * @see <a href=
		 *      "https://docs.gitlab.com/ee/ci/variables/#predefined-variables-environment-variables">GitLab
		 *      predefined variables</a>
		 */
		private static final String EXECUTED_BY_GITLAB_CI = "CI";

		/** The original statement/test that JUnit will execute. */
		private final Statement original;

		/** The statement's description. */
		private final Description description;

		/** Constructor. */
		private RetryStatement(Statement original, Description description) {
			this.original = original;
			this.description = description;
		}

		@Override
		public void evaluate() throws Throwable {
			if (environment.get(EXECUTED_BY_GITLAB_CI) != null) {
				// Running on CI server
				evaluateWithRetries();
			} else {
				// Running locally
				original.evaluate();
			}
		}

		private void evaluateWithRetries() throws Throwable {
			int retryCount = -1;
			List<Throwable> failures = new ArrayList<>();
			do {
				retryCount++;
				try {
					original.evaluate();
					break; // Successful test execution
				} catch (Throwable t) {
					failures.add(t);
				}
			} while (retryCount < maxRetryCount);

			announceVerdict(description, retryCount, failures);
		}

		private void announceVerdict(Description description, int retryCount, List<Throwable> failures)
				throws Throwable {
			int failureCount = failures.size();
			if (failureCount == 0) {
				return;
			} else if (failureCount == 1 + retryCount) {
				// All attempts (initial + retries) have failed. Very likely a real test failure
				// and not a flickering test.
				throw exceptionSummarizingFailures(failures);
			} else {
				System.err.println(String.format("Test %s is flickering; %d of %d attempts failed.",
						description.getDisplayName(), failureCount, 1 + retryCount));
				Throwable summaryException = exceptionSummarizingFailures(failures);
				summaryException.printStackTrace();
				for (Throwable f : failures) {
					f.printStackTrace();
				}
			}
		}

		private Throwable exceptionSummarizingFailures(List<Throwable> failures) throws Throwable {
			if (failures.size() == 1) {
				return failures.get(0);
			}
			return new MultipleFailureException(failures);
		}
	}
}
