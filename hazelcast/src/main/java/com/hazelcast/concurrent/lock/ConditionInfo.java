/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.concurrent.lock;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @mdogan 2/14/13
 */
class ConditionInfo implements DataSerializable {

    private String conditionId;
    private Set<ConditionWaiter> waiters = new HashSet<ConditionWaiter>(2);

    public ConditionInfo() {
    }

    public ConditionInfo(String conditionId) {
        this.conditionId = conditionId;
    }

    public boolean addWaiter(String caller, int threadId) {
        return waiters.add(new ConditionWaiter(caller, threadId));
    }

    public boolean removeWaiter(String caller, int threadId) {
        return waiters.remove(new ConditionWaiter(caller, threadId));
    }

    public String getConditionId() {
        return conditionId;
    }

    public int getAwaitCount() {
        return waiters.size();
    }

    public boolean containsWaiter(String caller, int threadId) {
        return waiters.contains(new ConditionWaiter(caller, threadId));
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(conditionId);
        int len = waiters.size();
        out.writeInt(len);
        if (len > 0) {
            for (ConditionWaiter w : waiters) {
                out.writeUTF(w.caller);
                out.writeInt(w.threadId);
            }
        }
    }

    public void readData(ObjectDataInput in) throws IOException {
        conditionId = in.readUTF();
        int len = in.readInt();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                waiters.add(new ConditionWaiter(in.readUTF(), in.readInt()));
            }
        }
    }

    static class ConditionWaiter {
        String caller;
        int threadId;

        ConditionWaiter() {
        }

        ConditionWaiter(String caller, int threadId) {
            this.caller = caller;
            this.threadId = threadId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConditionWaiter that = (ConditionWaiter) o;

            if (threadId != that.threadId) return false;
            if (caller != null ? !caller.equals(that.caller) : that.caller != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = caller != null ? caller.hashCode() : 0;
            result = 31 * result + threadId;
            return result;
        }
    }
}
