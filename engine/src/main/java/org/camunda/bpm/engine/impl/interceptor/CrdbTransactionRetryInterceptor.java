/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;

public class CrdbTransactionRetryInterceptor extends CommandInterceptor {

  private static final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  // TODO: how to address this in the managed tx case? should not make a retry
  //   if the tx is externally managed, but ideally we do make a retry if the
  //   tx is begun when the Camunda command starts
  
  protected int retries;
  
  public CrdbTransactionRetryInterceptor(int retries) {
    this.retries = retries;
  }
  
  @Override
  public <T> T execute(Command<T> command) {
    int remainingTries = retries + 1;
    while (remainingTries > 0) {
      try {
        // delegate to next interceptor in chain
        return next.execute(command);
      } catch (CrdbTransactionRetryException e) {
        remainingTries--;
        if (!command.isRetryable() || remainingTries == 0) {
          throw e;
        } else {
          LOG.crdbTransactionRetryAttempt(e);
        }
      }
    }

    return null;
  }
}